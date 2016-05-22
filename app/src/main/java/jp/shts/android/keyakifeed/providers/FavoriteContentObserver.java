package jp.shts.android.keyakifeed.providers;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class FavoriteContentObserver extends ContentObserver {

    private static final String TAG = FavoriteContentObserver.class.getSimpleName();

    @IntDef({State.UNKNOWN, State.ADD, State.REMOVE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
        int UNKNOWN = 0;
        int ADD = 1;
        int REMOVE = 2;
    }

    public FavoriteContentObserver() {
        super(new Handler());
    }

    public void register(@NonNull Context context) {
        context.getContentResolver().registerContentObserver(
                KeyakiFeedContent.Favorite.CONTENT_URI, true, this);
    }

    public void unregister(@NonNull Context context) {
        context.getContentResolver().unregisterContentObserver(this);
    }

    /**
     * DBに更新がかかった時に通知される
     * <p/>
     * ADD(content://jp.shts.android.keyakifeed.providers.keyakifeed/favorite/44)
     * REMOVE(content://jp.shts.android.keyakifeed.providers.keyakifeed/favorite)
     *
     * @param selfChange
     * @param uri
     */
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        // ADDした時はpathの最後にIDが付与されるのでそれ以外はREMOVE
        if (uri != null && "favorite".equals(uri.getLastPathSegment())) {
            notify(State.REMOVE);
        } else {
            notify(State.ADD);
        }
    }

    private void notify(final @State int state) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                onChangeState(state);
            }
        });
    }

    public abstract void onChangeState(@State int state);
}
