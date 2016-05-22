package jp.shts.android.keyakifeed.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

public class KeyakiFeedProvider extends ContentProvider {

    private static final String TAG = KeyakiFeedProvider.class.getSimpleName();

    private static final int FAVORITE = 1;
    private static final int UNREAD = 2;

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(KeyakiFeedContent.AUTHORITY, KeyakiFeedContent.TABLE_FAVORITE, FAVORITE);
        URI_MATCHER.addURI(KeyakiFeedContent.AUTHORITY, KeyakiFeedContent.TABLE_UNREAD, UNREAD);
    }

    private KeyakiFeedDatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new KeyakiFeedDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String orderBy;
        switch (URI_MATCHER.match(uri)) {
            case FAVORITE:
                qb.setTables(KeyakiFeedContent.TABLE_FAVORITE);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = KeyakiFeedContent.Favorite.Key.ID + " DESC";
                } else {
                    orderBy = sortOrder;
                }
                break;
            case UNREAD:
                qb.setTables(KeyakiFeedContent.TABLE_UNREAD);
                if (TextUtils.isEmpty(sortOrder)) {
                    orderBy = KeyakiFeedContent.UnRead.Key.ID + " DESC";
                } else {
                    orderBy = sortOrder;
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs,
                null, null, orderBy);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case FAVORITE:
                return KeyakiFeedContent.Favorite.CONTENT_TYPE;
            case UNREAD:
                return KeyakiFeedContent.UnRead.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        if ((URI_MATCHER.match(uri) != FAVORITE)
                && (URI_MATCHER.match(uri) != UNREAD)) {
            throw new IllegalArgumentException("Unknown URL *** " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        SQLiteDatabase db = databaseHelper.getWritableDatabase();

        long rowID;
        switch (URI_MATCHER.match(uri)) {
            case FAVORITE:
                rowID = db.replace(KeyakiFeedContent.TABLE_FAVORITE, "NULL", values);

                if (rowID > 0) {
                    Uri newUri = ContentUris.withAppendedId(KeyakiFeedContent.Favorite.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;
            case UNREAD:
                rowID = db.replace(KeyakiFeedContent.TABLE_UNREAD, "NULL", values);

                if (rowID > 0) {
                    Uri newUri = ContentUris.withAppendedId(KeyakiFeedContent.UnRead.CONTENT_URI, rowID);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    return newUri;
                }
                break;
        }
        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int count;

        switch (URI_MATCHER.match(uri)) {
            case FAVORITE:
                if (selection != null || selectionArgs != null) {
                    count = db.delete(KeyakiFeedContent.TABLE_FAVORITE, selection, selectionArgs);
                } else {
                    count = db.delete(KeyakiFeedContent.TABLE_FAVORITE, " "
                            + KeyakiFeedContent.Favorite.Key.ID + " like '%'", null);
                }
                break;
            case UNREAD:
                if (selection != null || selectionArgs != null) {
                    count = db.delete(KeyakiFeedContent.TABLE_UNREAD, selection, selectionArgs);
                } else {
                    count = db.delete(KeyakiFeedContent.TABLE_UNREAD, " "
                            + KeyakiFeedContent.UnRead.Key.ID + " like '%'", null);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        int count;
        switch (URI_MATCHER.match(uri)) {
            case FAVORITE:
                count = db.update(KeyakiFeedContent.TABLE_FAVORITE, values, selection, selectionArgs);
                break;
            case UNREAD:
                count = db.update(KeyakiFeedContent.TABLE_UNREAD, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }
}
