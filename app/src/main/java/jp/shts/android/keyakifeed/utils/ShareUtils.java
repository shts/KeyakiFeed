package jp.shts.android.keyakifeed.utils;

import android.content.Intent;

import jp.shts.android.keyakifeed.models.Entry;

public class ShareUtils {

    private static final String TAG = ShareUtils.class.getSimpleName();

    private static final String BASE_URL = "http://keyakizaka46-mirror.herokuapp.com/entry/show/";

    public static Intent getShareBlogIntent(Entry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getAuthor()).append(" | ").append(entry.getTitle()).append("\n");
        sb.append(BASE_URL).append(entry.getObjectId());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());

        return intent;
    }
}
