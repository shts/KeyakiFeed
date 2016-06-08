package jp.shts.android.keyakifeed.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.BuildConfig;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.notifications.DownloadNotification;
import jp.shts.android.keyakifeed.utils.ImageUtils;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.functions.Func1;

public class DownloadImageService extends IntentService {

    private static final String TAG = DownloadImageService.class.getSimpleName();

    @NonNull
    public static Intent getStartIntent(@NonNull Context context, int memberId) {
        Intent intent = new Intent(context, DownloadImageService.class);
        intent.putExtra("memberId", memberId);
        return intent;
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public DownloadImageService(String name) {
        super(name);
    }

    public DownloadImageService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int memberId = intent.getIntExtra("memberId", -1);
        if (memberId < 0) {
            // 起こり得ないケース
            return;
        }

        ArrayList<String> targets = new ArrayList<>();
        int counter = 0;
        while (true) {
            List<String> list = KeyakiFeedApiClient
                    .getMemberEntries(memberId, (counter * 30), 30)
                    .map(new Func1<Entries, List<String>>() {
                        @Override
                        public List<String> call(Entries entries) {
                            return entries.getImageUrlList();
                        }
                    })
                    .toBlocking().single();
            if (list == null || list.isEmpty()) break;
            targets.addAll(list);
            counter++;
        }
        downloadImage(targets);
    }

    public void downloadImage(@NonNull List<String> urlList) {
        final DownloadNotification notification
                = new DownloadNotification(this, urlList.size());

        List<Request> requests = createRequest(urlList);
        // start show notification
        notification.startProgress();

        for (Request request : requests) {
            try {
                Response response = createOkHttpClient().newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("failed to download");

                InputStream inputStream = response.body().byteStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                String filePath = SdCardUtils.getDownloadFilePath(request.url().toString());
                File outputFile = ImageUtils.saveBitmapFile(bitmap, filePath);
                Uri uri = Uri.fromFile(outputFile);
                startMediaScan(uri);
                notification.updateProgress(uri);

            } catch (IOException e) {
                e.printStackTrace();
                notification.updateProgress(null);
            }
        }
    }

    private void startMediaScan(Uri uri) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    @NonNull
    private List<Request> createRequest(@NonNull List<String> urlList) {
        List<Request> requests = new ArrayList<>();
        for (String url : urlList) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            requests.add(request);
        }
        return requests;
    }

    @NonNull
    private OkHttpClient createOkHttpClient() {
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            return new OkHttpClient.Builder()
                    .addNetworkInterceptor(logging)
                    .build();
        } else {
            return new OkHttpClient.Builder().build();
        }
    }
}
