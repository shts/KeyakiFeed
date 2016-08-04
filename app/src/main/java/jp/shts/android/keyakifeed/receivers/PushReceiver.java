package jp.shts.android.keyakifeed.receivers;

import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Push receiver for FCM
 */
public class PushReceiver extends FirebaseMessagingService {

    private static final String TAG = PushReceiver.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        String from = remoteMessage.getFrom();
        Map<String,String> data = remoteMessage.getData();
        Log.d(TAG, "onMessageReceived: from(" + from + ")");

        Bundle bundle = new Bundle();
        for (Map.Entry<String,String> ent : data.entrySet()) {
            bundle.putString(ent.getKey(), ent.getValue());
            /**
             * "data" => { "message" => "GCM Demo",
             *             "detail" => "Hello world"}
             *
             * key(message) value(GCM Demo)
             * key(detail) value(Hello world)
             */
            Log.d(TAG, "onMessageReceived: key(" + ent.getKey()
                    + ") value(" + ent.getValue() + ")");
            // TODO: parse
            // key(push_type)でパースするオブジェクトを判断する
        }
    }
}
