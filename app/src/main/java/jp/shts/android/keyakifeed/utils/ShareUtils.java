package jp.shts.android.keyakifeed.utils;

import android.content.Intent;

import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.entities.FeedItem;

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

    public static Intent getShareMatomeBlogIntent(FeedItem feedItem) {
        StringBuilder sb = new StringBuilder();
        sb.append(feedItem.title).append(" | ");
        sb.append(feedItem.siteTitle).append("\n");
        sb.append(feedItem.url);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        return intent;
    }
}
