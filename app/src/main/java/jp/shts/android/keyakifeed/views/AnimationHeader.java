package jp.shts.android.keyakifeed.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ViewAnimator;

import jp.shts.android.keyakifeed.R;

public class AnimationHeader extends FrameLayout {

    private static final String TAG = AnimationHeader.class.getSimpleName();

    public AnimationHeader(Context context) {
        super(context, null);
    }
    public AnimationHeader(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }
    public AnimationHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimationHeader(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init() {
        ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.animator);

    }

    public void setupImages() {

    }

}
