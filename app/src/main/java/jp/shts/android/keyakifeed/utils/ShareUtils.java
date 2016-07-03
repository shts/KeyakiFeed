package jp.shts.android.keyakifeed.utils;

import android.content.Intent;
import android.support.annotation.NonNull;

import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Matome;

public class ShareUtils {

    private static final String TAG = ShareUtils.class.getSimpleName();

    public static Intent getShareBlogIntent(Blog blog) {
        StringBuilder sb = new StringBuilder();
        sb.append(blog.getMemberName()).append(" | ").append(blog.getTitle()).append("\n");
        sb.append(blog.getUrl());

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
