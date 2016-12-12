package jp.shts.android.keyakifeed.views;

import android.content.Context;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import jp.shts.android.keyakifeed.R;

import static android.os.Looper.getMainLooper;

public class HackySwipeRefreshLayout extends SwipeRefreshLayout {

    private static final String TAG = HackySwipeRefreshLayout.class.getSimpleName();

    public HackySwipeRefreshLayout(Context context) {
        super(context);
        init();
    }

    public HackySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setColorSchemeResources(R.color.primary);
    }

    private final Handler refreshHandler = new Handler(getMainLooper());
    private final Runnable refreshTask = new Runnable() {
        @Override
        public void run() {
            HackySwipeRefreshLayout.super.setRefreshing(true);
        }
    };

    @Override
    public void setRefreshing(boolean refreshing) {
        if (isRunUiThread()) {
            super.setRefreshing(refreshing);
        } else {
            if (refreshing) {
                refreshHandler.post(refreshTask);
            } else {
                refreshHandler.removeCallbacks(refreshTask);
                super.setRefreshing(refreshing);
            }
        }
    }

    private boolean isRunUiThread() {
        return Thread.currentThread().equals(getMainLooper().getThread());
    }
}
