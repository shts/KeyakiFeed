package jp.shts.android.keyakifeed;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.parse.Parse;
import com.parse.ParseInstallation;

import io.fabric.sdk.android.Fabric;

public class KeyakiFeedApplication extends Application {

    private static final String TAG = KeyakiFeedApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Parse.initialize(this, BuildConfig.PARSE_API_ID, BuildConfig.PARSE_API_KEY);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
