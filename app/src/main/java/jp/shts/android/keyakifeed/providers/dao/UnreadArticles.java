package jp.shts.android.keyakifeed.providers.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import jp.shts.android.keyakifeed.providers.KeyakiFeedContent;

public class UnreadArticles extends ArrayList<UnreadArticle> {

    private static final String TAG = UnreadArticles.class.getSimpleName();

    public static UnreadArticles all(@NonNull Context context) {
        UnreadArticles unreadArticles = new UnreadArticles();
        Cursor c = context.getContentResolver().query(
                KeyakiFeedContent.UnRead.CONTENT_URI, null, null, null, null);
        if (c == null || !c.moveToFirst()) return unreadArticles;
        try {
            do {
                int id = c.getInt(c.getColumnIndex(KeyakiFeedContent.UnRead.Key.ID));
                int memberId = c.getInt(c.getColumnIndex(KeyakiFeedContent.UnRead.Key.MEMBER_ID));
                String url = c.getString(c.getColumnIndex(KeyakiFeedContent.UnRead.Key.ARTICLE_URL));
                unreadArticles.add(new UnreadArticle(id, memberId, url));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return unreadArticles;
    }

    public static void add(@NonNull Context context, int memberId, @NonNull String url) {
        ContentValues cv = new ContentValues();
        cv.put(KeyakiFeedContent.UnRead.Key.MEMBER_ID, memberId);
        cv.put(KeyakiFeedContent.UnRead.Key.ARTICLE_URL, url);
        context.getContentResolver().insert(KeyakiFeedContent.UnRead.CONTENT_URI, cv);
    }

    public static void remove(@NonNull Context context, @NonNull String url) {
        String selection = KeyakiFeedContent.UnRead.Key.ARTICLE_URL + "=?";
        String[] selectionArgs = {url};
        context.getContentResolver().delete(KeyakiFeedContent.UnRead.CONTENT_URI, selection, selectionArgs);
    }

    public static boolean exist(@NonNull Context context, @NonNull String url) {
        String selection = KeyakiFeedContent.UnRead.Key.ARTICLE_URL + "=?";
        String[] selectionArgs = {url};

        Cursor c = context.getContentResolver().query(
                KeyakiFeedContent.UnRead.CONTENT_URI,
                KeyakiFeedContent.UnRead.sProjection,
                selection, selectionArgs, null);
        if (c == null || !c.moveToFirst()) return false;
        try {
            return c.getCount() == 1;
        } finally {
            c.close();
        }
    }

}
