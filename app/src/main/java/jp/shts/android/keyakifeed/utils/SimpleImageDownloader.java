package jp.shts.android.keyakifeed.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleImageDownloader extends ImageDownloader {

    private static final String TAG = SimpleImageDownloader.class.getSimpleName();

    public SimpleImageDownloader(Context context, String url) {
        super(context, new ArrayList<>(Arrays.asList(url)));
    }

    public SimpleImageDownloader(Context context, List<String> urls) {
        super(context, urls);
    }

    @Override
    public void onResponse(Response response) {
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
}
