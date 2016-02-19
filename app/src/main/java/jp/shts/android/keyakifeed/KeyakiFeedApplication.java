package jp.shts.android.keyakifeed;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;

import jp.shts.android.keyakifeed.adapters.MemberFeedListAdapter2;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.Member;

public class KeyakiFeedApplication extends Application {

    private static final String TAG = KeyakiFeedApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(Entry.class);
        ParseObject.registerSubclass(Favorite.class);
        ParseObject.registerSubclass(Member.class);

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, BuildConfig.PARSE_API_ID, BuildConfig.PARSE_API_KEY);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }
}
