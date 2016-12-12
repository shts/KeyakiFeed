package jp.shts.android.keyakifeed.utils;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageUtils {

    private static final String TAG = ImageUtils.class.getSimpleName();

    @NonNull
    public static File saveBitmapFile(@NonNull Bitmap bitmap, @NonNull String filePath) throws IOException {
        File outputFile = new File(filePath);
        OutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                throw new IOException("creating bitmap file failed");
            }
            return outputFile;
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
}
