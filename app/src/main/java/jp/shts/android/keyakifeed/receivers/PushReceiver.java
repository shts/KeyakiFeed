package jp.shts.android.keyakifeed.receivers;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Report;
import jp.shts.android.keyakifeed.notifications.BlogUpdateNotification;
import jp.shts.android.keyakifeed.providers.dao.UnreadArticles;

/**
 * Push receiver for FCM
 */
public class PushReceiver extends FirebaseMessagingService {

    private static final String TAG = PushReceiver.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String,String> data = remoteMessage.getData();

        Gson g = new Gson(); String json = g.toJson(data);

        if (data.containsValue("object_entry")) {
            Entry e = g.fromJson(json, Entry.class);
            if (e != null) {
                BlogUpdateNotification.showExecUiThread(this, e);
                UnreadArticles.add(this, e.getMemberId(), e.getUrl());
            }

        } else if (data.containsValue("object_report")) {
            Report r = g.fromJson(json, Report.class);
            if (r != null) {
                BlogUpdateNotification.showExecUiThread(this, r.toEntry());
                UnreadArticles.add(this, r.getUrl());
            }
        }
    }
}
