package jp.shts.android.keyakifeed.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DeepLinkActivity extends Activity {

    private static final String TAG = DeepLinkActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String entryObjectId = getIntent()
                .getDataString().replace("http://keyakizaka46-mirror.herokuapp.com/entry/show/", "");
        Intent i = BlogActivity.getStartIntent(this, entryObjectId);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        finish();
    }
}