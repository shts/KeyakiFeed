package jp.shts.android.keyakifeed.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.notifications.BlogUpdateNotification;

public class BlogUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = BlogUpdateReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "in : intent(" + intent.toUri(Intent.URI_INTENT_SCHEME) + ")");
        try {
            // jsonから値を取り出し
            Bundle extra = intent.getExtras();
            String data = extra.getString("com.parse.Data");
            JSONObject json = new JSONObject(data);

            final String action = intent.getAction();
            if ("jp.shts.android.keyakifeed.BLOG_UPDATED".equals(action)) {
                Blog blog = new Blog(json);
                BlogUpdateNotification.show(context, blog);

            } else if ("jp.shts.android.keyakifeed.REPORT_UPDATED".equals(action)) {
                // 取り出したデータを変数へ
                final String entryObjectId = json.getString("_entryObjectId");
                final String title = json.getString("_title");
                final String url = json.getString("_url");
                final String thumbnailUrl = json.getString("_thumbnail_url");

//                BlogUpdateNotification.show(context, entryObjectId,
//                        title, author, author_id, author_image_url);
            }

        } catch (JSONException e) {
            Log.e(TAG, "cannot get entryObjectId", e);
        }
    }
}
