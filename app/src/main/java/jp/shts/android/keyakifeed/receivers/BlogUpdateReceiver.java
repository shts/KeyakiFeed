package jp.shts.android.keyakifeed.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
            // 取り出したデータを変数へ
            final String entryObjectId = json.getString("_entryObjectId");
            final String title = json.getString("_title");
            final String author = json.getString("_author");
            final String author_id = json.getString("_author_id");
            final String author_image_url = json.getString("_author_image_url");

            BlogUpdateNotification.show(context, entryObjectId,
                title, author, author_id, author_image_url);

        } catch (JSONException e) {
            Log.e(TAG, "cannot get entryObjectId", e);
        }
    }
}
