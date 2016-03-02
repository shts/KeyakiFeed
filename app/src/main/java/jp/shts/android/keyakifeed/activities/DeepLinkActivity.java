package jp.shts.android.keyakifeed.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class DeepLinkActivity extends Activity {

    private static final String TAG = DeepLinkActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO:
        // http://www.keyakizaka46.com/mob/news/diarKijiShw.php?site=k46o&ima=0445&id=405&cd=member
        String url = getIntent().getDataString();
        String[] query = url.split("&");
        String id = query[query.length - 2].replace("id=", "");

//        final String entryObjectId = getIntent()
//                .getDataString().replace("http://keyakizaka46-mirror.herokuapp.com/entry/show/", "");
//        Intent i = BlogActivity.getStartIntent(this, entryObjectId);
//        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(i);
        finish();
    }
}
