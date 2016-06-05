package jp.shts.android.keyakifeed.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.util.List;

import jp.shts.android.keyakifeed.notifications.DownloadNotification;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;

public class DownloadImageService extends IntentService {

    private static final String TAG = DownloadImageService.class.getSimpleName();

    public static void download(Context context, String memberObjectId) {
        Intent intent = new Intent(context, DownloadImageService.class);
        intent.putExtra("memberObjectId", memberObjectId);
        context.startService(intent);
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DownloadImageService(String name) { super(name); }

    public DownloadImageService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO:
        //BusHolder.get().register(this);
    }

    @Override
    public void onDestroy() {
        //BusHolder.get().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(TAG, "onHandleIntent(Intent) in : memberObjectId("
                + intent.getStringExtra("memberObjectId") + ")");
        // TODO:
//        ParseQuery<Entry> query = ParseQuery.getQuery(Entry.class);
//        query.orderByDescending("published");
//        query.whereEqualTo("member_id", intent.getStringExtra("memberObjectId"));
//        query.findInBackground(new FindCallback<Entry>() {
//            @Override
//            public void done(List<Entry> entries, ParseException e) {
//                Log.v(TAG, "onGotAllImage");
//                ArrayList<String> targets = new ArrayList<>();
//                for (Entry entry : entries) {
//                    targets.addAll(entry.getImageUrlList());
//                }
//                download(targets);
//            }
//        });
    }

    public void download(List<String> urlList) {
        Log.v(TAG, "download");
        final DownloadNotification notification
                = new DownloadNotification(this, urlList.size());
        new SimpleImageDownloader(this, urlList) {
            @Override
            public void onStart() {
                super.onStart();
                Log.v(TAG, "onStart");
                // start show notification
                notification.startProgress();
            }
            @Override
            public void onResponse(Response response) {
                super.onResponse(response);
                Log.v(TAG, "onResponse");
                // count show notification
                if (response.file == null) {
                    notification.updateProgress(null);

                } else {
                    SdCardUtils.scanFile(DownloadImageService.this, response.file,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    notification.updateProgress(uri);
                                }
                     });
                }
            }
        }.get();
    }
}
