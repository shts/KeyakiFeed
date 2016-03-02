package jp.shts.android.keyakifeed.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.utils.PreferencesUtils;
import jp.shts.android.keyakifeed.views.transformations.CircleTransformation;

public class BlogUpdateNotification {

    private static final String TAG = BlogUpdateNotification.class.getSimpleName();

    public static final String KEY = BlogUpdateNotification.class.getSimpleName();

    private static final String NOTIFICATION_ID_KEY = "pref_key_blog_update_notification_id";
    private static final int DEFAULT_NOTIFICATION_ID = 1000;

    /** ブログ更新通知可否設定 */
    private static final String NOTIFICATION_ENABLE = "pref_key_blog_updated_notification_enable";
    /** ブログ更新通知制限設定(お気に入りメンバーのみ通知する設定) */
    private static final String NOTIFICATION_RESTRICTION_ENABLE = "pref_key_blog_updated_notification_restriction_enable";

    public static void show(Context context, Blog blog) {

        final boolean isEnableNotification
                = PreferencesUtils.getBoolean(context, NOTIFICATION_ENABLE, true);
        if (!isEnableNotification) {
            Log.d(TAG, "do not show notification because of notification disable");
            return;
        }

        if (isRestriction(context, blog.getMemberId())) {
            Log.d(TAG, "do not show notification because of notification restriction");
            return;
        }

        Intent intent = BlogActivity.getStartIntent(context, blog);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final int notificationId = getNotificationId(context);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification_blog_update);
        views.setTextViewText(R.id.title, blog.getTitle());
        views.setTextViewText(R.id.text, blog.getMemberName());
        Notification notification = new NotificationCompat.Builder(context)
                .setContentIntent(contentIntent)
                .setContent(views)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .build();

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        ((NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE)).notify(notificationId, notification);

        notified(context, notificationId);

        if (!TextUtils.isEmpty(blog.getMemberImageUrl())) {
            Picasso.with(context).load(blog.getMemberImageUrl())
                    .placeholder(R.drawable.ic_account_circle_black_48dp)
                    .transform(new CircleTransformation()).into(
                    views, R.id.icon, notificationId, notification);
        }
    }

    private static boolean isRestriction(Context context, String authorId) {
        final boolean isRestriction = PreferencesUtils.getBoolean(
                context, NOTIFICATION_RESTRICTION_ENABLE, false);
        if (!isRestriction) {
            // 通知制限設定をしていない場合はそのまま通知するようfalseを返却する
            Log.d(TAG, "restriction is not setting");
            return false;
        }
        final boolean exist = Favorite.exist(authorId);
        // お気に入りメンバー登録済みの場合false, お気に入りメンバー登録済みでない場合trueを返却する
        Log.d(TAG, "restriction exist(" + exist + ")");
        return !exist;
    }

    private static int getNotificationId(Context context) {
        return PreferencesUtils.getInt(context, NOTIFICATION_ID_KEY, DEFAULT_NOTIFICATION_ID);
    }

    private static void notified(Context context, int id) {
        if (++id >= 1999) {
            id = DEFAULT_NOTIFICATION_ID;
        }
        PreferencesUtils.setInt(context, NOTIFICATION_ID_KEY, id);
    }

}
