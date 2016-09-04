package jp.shts.android.keyakifeed.receivers;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Report;
import jp.shts.android.keyakifeed.notifications.BlogUpdateNotification;

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
            if (e != null) BlogUpdateNotification.showExecUiThread(this, e);

        } else if (data.containsValue("object_report")) {
            Report r = g.fromJson(json, Report.class);
            if (r != null) BlogUpdateNotification.showExecUiThread(this, r.toEntry());
        }
    }
}
