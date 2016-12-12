package jp.shts.android.keyakifeed.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * https://github.com/chrisbanes/PhotoView/blob/master/sample/src/main/java/uk/co/senab/photoview/sample/HackyViewPager.java
 */
public class HackyViewPager extends ViewPager {

    private static final String TAG = HackyViewPager.class.getSimpleName();

    public HackyViewPager(Context context) {
        super(context);
    }

    public HackyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}
