package jp.shts.android.keyakifeed.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import jp.shts.android.keyakifeed.BuildConfig;

public class KeyakiFeedAdView extends LinearLayout {

    private static final String TAG = KeyakiFeedAdView.class.getSimpleName();

    private AdView adView;

    public KeyakiFeedAdView(Context context) {
        this(context, null);
    }

    public KeyakiFeedAdView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyakiFeedAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KeyakiFeedAdView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void pause() { if (adView != null) adView.pause(); }
    public void resume() { if (adView != null) adView.resume(); }
    public void destroy() { if (adView != null) adView.destroy(); }

    private void init(Context context) {
        setGravity(Gravity.CENTER_HORIZONTAL);

        adView = new AdView(context);
        adView.setAdUnitId(BuildConfig.AD_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);
        adView.loadAd(buildRequest());

        addView(adView);
    }

    private AdRequest buildRequest() {
        if (BuildConfig.DEBUG) {
            return new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
        } else {
            return new AdRequest.Builder().build();
        }
    }
}
