package jp.shts.android.keyakifeed.utils;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * 設定された秒数までコールバックを待ち合わせる画像ダウンローダー
 * ダウンロードが早く終了しチラついて見える現象を回避するため
 */
public class WaitMinimunImageDownloader extends ImageDownloader {

    private static final String TAG = WaitMinimunImageDownloader.class.getSimpleName();

    public WaitMinimunImageDownloader(Context context, List<String> urls) { super(context, urls);}

    private boolean isTimeUp = false;
    private boolean isFinishDownload = false;
    private List<Response> responseList;
    private final Object LOCK = new Object();

    @Override
    final public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        isTimeUp = false;
        isFinishDownload = false;
        new android.os.Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "time up");
                synchronized (LOCK) {
                    isTimeUp = true;
                    if (isFinishDownload) {
                        onFinish(responseList);
                    }
                }
            }
        }, getWaitTime());
    }

    @Override
    final public void onResponse(Response response) {
        Log.i(TAG, "onResponse");
        if (response.result == Response.Result.SUCCESS) {
            onResponse(response.file);
        } else {
            Log.e(TAG, "failed to download image : response("
                    + response.toString() + ")");
        }
    }

    public void onResponse(File file) {
        Log.v(TAG, "image downloaded : file("
                + (file == null ? "null" : file.getAbsolutePath()) + ")");
    }

    @Override
    final public void onComplete(List<Response> responseList) {
        Log.i(TAG, "onComplete");
        this.responseList = responseList;
        synchronized (LOCK) {
            isFinishDownload = true;
            if (isTimeUp) {
                onFinish(responseList);
            }
        }
    }

    /**
     * 待ち合わせも含みすべてのダウンロードが完了
     */
    public void onFinish(List<Response> responseList) {}

    /**
     * 待ち合わせ時間(ms)
     * @return デフォルトは1秒
     */
    public int getWaitTime() {
        return 3000;
    }

}
