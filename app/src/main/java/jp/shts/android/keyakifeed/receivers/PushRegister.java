package jp.shts.android.keyakifeed.receivers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import retrofit2.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class PushRegister {

    private static final String TAG = PushRegister.class.getSimpleName();

    private static class Store {
        private static final String REG_PREF = "reg_pref";
        private static final String REG_ID = "reg_id";

        private static SharedPreferences getPref(@NonNull Context context) {
            return context.getSharedPreferences(REG_PREF, Context.MODE_PRIVATE);
        }

        @SuppressLint("CommitPrefEdits")
        private static void setRegId(@NonNull Context context, @NonNull String regId) {
            getPref(context).edit().putString(REG_ID, regId).commit();
        }

        @Nullable
        private static String getRegId(@NonNull Context context) {
            return getPref(context).getString(REG_ID, null);
        }
    }

    public static void init(Context context, boolean isForceTokenRefresh) {
        final String regId = Store.getRegId(context);
        if (TextUtils.isEmpty(regId) || isForceTokenRefresh) {
            onTokenRefresh(context);
        }
    }

    /**
     * FCMのトークンを取得する
     * <p>
     * 初回起動時にnullの場合があるのでリトライする
     *
     * @return FCMのトークン
     */
    private static Observable<String> createGetTokenObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    String regId = FirebaseInstanceId.getInstance().getToken();
                    Log.d(TAG, "call: regId(" + regId + ")");
                    // null の場合があるのでリトライするためExceptionをなげる
                    if (TextUtils.isEmpty(regId)) {
                        throw new Throwable();
                    }
                    subscriber.onNext(regId);
                    subscriber.onCompleted();
                } catch (Throwable throwable) {
                    subscriber.onError(throwable);
                }
            }
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(final Observable<? extends Throwable> observable) {
                return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        Log.d(TAG, "call: Retry get token");
                        // 3秒後にリトライする
                        return Observable.timer(3, TimeUnit.SECONDS);
                    }
                });
            }
        });
    }

    /**
     * 古いトークンを削除して、新しいトークンを登録する
     *
     * @param regId    　新しいトークン
     * @param oldRegId 古いトークン
     * @return
     */
    private static Observable<?> createTokenUpdateObservable(@NonNull String regId,
                                                             @NonNull String oldRegId) {
        return Observable.combineLatest(
                KeyakiFeedApiClient.unregistrationId(oldRegId)
                        .onErrorReturn(new Func1<Throwable, Void>() {
                            @Override
                            public Void call(Throwable throwable) {
                                Log.d(TAG, "Failed to unregistrationId: throwable(" + throwable + ")");
                                throwable.printStackTrace();
                                return null;
                            }
                        }).subscribeOn(Schedulers.newThread()),
                KeyakiFeedApiClient.registrationId(regId)
                        .onErrorReturn(new Func1<Throwable, Void>() {
                            @Override
                            public Void call(Throwable throwable) {
                                Log.d(TAG, "Failed to registrationId: throwable(" + throwable + ")");
                                throwable.printStackTrace();
                                return null;
                            }
                        }).subscribeOn(Schedulers.newThread()),
                new Func2<Void, Void, Object>() {
                    @Override
                    public Object call(Void aVoid, Void aVoid2) {
                        return null;
                    }
                });
    }

    static void onTokenRefresh(@NonNull final Context context) {
        createGetTokenObservable()
                .flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(final String regId) {
                        // getToken()で取得した値が空の場合
                        if (TextUtils.isEmpty(regId)) return null;

                        String oldRegId = Store.getRegId(context);
                        // 急tokenが空の場合は必ず登録する
                        if (TextUtils.isEmpty(oldRegId)) {
                            Store.setRegId(context, regId);
                            return KeyakiFeedApiClient.registrationId(regId);
                        }

                        // Idが更新された場合は既存のIDを解除後、再度登録する
                        if (!regId.equals(oldRegId)) {
                            Store.setRegId(context, regId);
                            return createTokenUpdateObservable(regId, oldRegId);
                        }

                        return Observable.empty();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        unsubscribe();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (e instanceof HttpException) {
                            HttpException httpException = (HttpException) e;
                            int statusCode = httpException.code();
                            // すでに登録されている場合 555
                            if (statusCode != 555 && statusCode != 500) {
                                //
                            }
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(Object o) {
                        unsubscribe();
                    }
                });
    }
}

