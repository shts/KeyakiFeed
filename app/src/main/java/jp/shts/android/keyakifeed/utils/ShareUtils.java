package jp.shts.android.keyakifeed.utils;

import android.content.Intent;
import android.support.annotation.NonNull;

import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Matome;

public class ShareUtils {

    private static final String TAG = ShareUtils.class.getSimpleName();

    public static Intent getShareBlogIntent(Entry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append(entry.getMemberName()).append(" | ").append(entry.getTitle()).append("\n");
        sb.append(entry.getUrl());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return intent;
    }

    @NonNull
    public static Intent getShareMatomeBlogIntent(@NonNull Matome matome) {
        StringBuilder sb = new StringBuilder();
        sb.append(matome.getEntryTitle()).append(" | ");
        sb.append(matome.getFeedTitle()).append("\n");
        sb.append(matome.getEntryUrl());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return intent;
    }
}
