package jp.shts.android.keyakifeed;

import android.app.Application;
import android.content.Intent;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import jp.shts.android.keyakifeed.receivers.TokenRegistrationService;

public class KeyakiFeedApplication extends Application {

    private static final String TAG = KeyakiFeedApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        startService(new Intent(this, TokenRegistrationService.class));
    }
}
