package jp.shts.android.keyakifeed.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;

public class SdCardUtils {

    private static final String TAG = SdCardUtils.class.getSimpleName();

    private static final String[] MIMETYPE = {
            "image/png", "image/jpg", "image/jpeg"
    };

    private SdCardUtils() { }

    /**
     * Reflect image to gallery.
     * @param context application context.
     * @param file file object.
     * @param listener callback for scan completed.
     */
    public static void scanFile(Context context, File file,
                                MediaScannerConnection.OnScanCompletedListener listener ) {
        MediaScannerConnection.scanFile(
                context, new String[] { file.getAbsolutePath() }, MIMETYPE, listener);
    }

    public static String getDownloadFilePath(String url) {
        final String[] splitedUrl = url.split("/");
        final String fileName = splitedUrl[splitedUrl.length - 1];
        return getDownloadFilePath() + File.separator + "download" + File.separator + fileName;
    }

    /**
     * Get android default 'download' dir path
     * @return download dir path.
     */
    private static String getDownloadFilePath() {
        File pathExternalPublicDir =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS);
        return pathExternalPublicDir.getPath();
    }
}
