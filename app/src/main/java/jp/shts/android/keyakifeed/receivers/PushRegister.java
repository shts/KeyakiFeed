package jp.shts.android.keyakifeed.receivers;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import retrofit2.HttpException;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class PushRegister {

    private static final String TAG = PushRegister.class.getSimpleName();

    public static class Store {
        private static final String REG_PREF = "reg_pref";
        private static final String REG_ID = "reg_id";

        private static SharedPreferences getPref(@NonNull Context context) {
            return context.getSharedPreferences(REG_PREF, Context.MODE_PRIVATE);
        }

        public static void setRegId(@NonNull Context context, @NonNull String regId) {
            getPref(context).edit().putString(REG_ID, regId).commit();
        }

        @Nullable
        public static String getRegId(@NonNull Context context) {
            return getPref(context).getString(REG_ID, null);
        }
    }

    public static void init(Context context, boolean isForceTokenRefresh) {
        final String regId = Store.getRegId(context);
        if (TextUtils.isEmpty(regId) || isForceTokenRefresh) {
            onTokenRefresh(context);
        }
    }

    public static void onTokenRefresh(@NonNull final Context context) {
        Observable.just(FirebaseInstanceId.getInstance().getToken())
                .flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<?> call(final String regId) {
                        Log.d(TAG, "call: regId(" + regId + ")");
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
                            return KeyakiFeedApiClient.unregistrationId(regId)
                                    .flatMap(new Func1<Void, Observable<?>>() {
                                        @Override
                                        public Observable<?> call(Void aVoid) {
                                            Store.setRegId(context, regId);
                                            return KeyakiFeedApiClient.registrationId(regId);
                                        }
                                    });
                        }
                        Store.setRegId(context, regId);
                        return KeyakiFeedApiClient.registrationId(regId);
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
                        if (e instanceof HttpException) {
                            HttpException httpException = (HttpException) e;
                            int statusCode = httpException.code();
                            // すでに登録されている場合 555
                            if (statusCode != 555 && statusCode != 500) {
                                // TODO: retry
                            }
                        }
                        unsubscribe();
                    }

                    @Override
                    public void onNext(Object o) {
                    }
                });
    }
}
