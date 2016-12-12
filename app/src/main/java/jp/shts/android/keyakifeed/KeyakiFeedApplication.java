package jp.shts.android.keyakifeed;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import jp.shts.android.keyakifeed.receivers.PushRegister;

public class KeyakiFeedApplication extends Application {

    private static final String TAG = KeyakiFeedApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        PushRegister.init(this, true);
    }
}
