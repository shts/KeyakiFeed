package jp.shts.android.keyakifeed.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.FileAsyncHttpResponseHandler;

import java.io.File;

import jp.shts.android.keyakifeed.notifications.DownloadNotification;

public class ImageDownloadClient {

    private static final String TAG = ImageDownloadClient.class.getSimpleName();

    private static AsyncHttpClient client = new AsyncHttpClient();

    private ImageDownloadClient() {}

    public static boolean get(final Context context, final String url) {

        if (!NetworkUtils.enableNetwork(context)) {
            Log.w(TAG, "cannot download because of network disconnected.");
            return false;
        }

        if (TextUtils.isEmpty(url)) {
            Log.w(TAG, "cannot download because of imageUrl is null.");
            return false;
        }

        final File file = new File(SdCardUtils.getDownloadFilePath(url));
        final DownloadNotification notification = new DownloadNotification(context, 1);
        client.get(url, new FileAsyncHttpResponseHandler(file) {
            @Override
            public void onStart() {
                super.onStart();
                notification.startProgress();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, File file) {
                notification.updateProgress(null);
            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, final File file) {
                SdCardUtils.scanFile(context, file, new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        notification.updateProgress(uri);
                    }
                });
            }
        });

        return true;
    }
}
