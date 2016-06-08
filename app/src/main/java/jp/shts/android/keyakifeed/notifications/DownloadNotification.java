package jp.shts.android.keyakifeed.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.utils.PreferencesUtils;

public class DownloadNotification {

    private static final String TAG = DownloadNotification.class.getSimpleName();

    private static final int DEFAULT_NOTIFICATION_ID = 2000;
    private static final String NOTIFICATION_ID_KEY = "pref_key_download_notification_id";

    private final Context context;

    private NotificationCompat.Builder notification = null;
    private NotificationManager notificationManager = null;

    private int counter = 0;
    private int maxSize = 0;
    private final int notificationId;
    private Uri lastDownloadedFileUri;

    public DownloadNotification(Context context, int targetSize) {
        this(context);
        maxSize = targetSize;
    }

    public DownloadNotification(Context context) {
        notificationId = getNotificationId(context);
        this.context = context;
        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * start notification progress.
     */
    public void startProgress() {
        notification = new NotificationCompat.Builder(context);
        notification.setSmallIcon(R.drawable.ic_file_download_white_24dp);
        Resources res = context.getResources();
        notification.setTicker(res.getString(R.string.notify_start_download_ticker));
        notification.setContentTitle(res.getString(R.string.notify_start_download_title));
        notification.setContentText(res.getString(R.string.notify_start_download_text));
        // クリック時に消去させない
        notification.setOngoing(true);

        notificationManager.notify(notificationId, notification.build());
    }

    /**
     * update notification progress.
     */
    public void updateProgress(Uri uri) {
        if (uri != null) lastDownloadedFileUri = uri;
        notification.setProgress(maxSize, counter++, false);
        notification.setContentText(counter + "/" + maxSize + " ...");
        notification.setOngoing(true);
        notificationManager.notify(notificationId, notification.build());

        if (maxSize <= counter) {
            finishProgress();
        }
    }

    private void finishProgress() {
        counter = 0;
        notification.setSmallIcon(R.drawable.ic_notification);
        Resources res = context.getResources();
        notification.setTicker(res.getString(R.string.notify_finish_download_ticker));
        notification.setContentTitle(res.getString(R.string.notify_finish_download_title));
        notification.setContentText(res.getString(R.string.notify_finish_download_text));
        notification.setContentIntent(getPendingIntentFrom(lastDownloadedFileUri));
        notification.setProgress(0, 0, false);
        notification.setOngoing(false);
        notification.setAutoCancel(true);
        notificationManager.notify(notificationId, notification.build());
        notified(context, notificationId);
    }

    private PendingIntent getPendingIntentFrom(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "image/jpeg");
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private static int getNotificationId(Context context) {
        return PreferencesUtils.getInt(context, NOTIFICATION_ID_KEY, DEFAULT_NOTIFICATION_ID);
    }

    private static void notified(Context context, int id) {
        if (++id >= 2999) {
            id = DEFAULT_NOTIFICATION_ID;
        }
        PreferencesUtils.setInt(context, NOTIFICATION_ID_KEY, id);
    }
}
