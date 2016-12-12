package jp.shts.android.keyakifeed.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class KeyakiFeedDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = KeyakiFeedDatabaseHelper.class.getSimpleName();

    //@formatter:off
    private static final String CREATE_FAVORITE_TABLE_SQL = "CREATE TABLE "
            + KeyakiFeedContent.TABLE_FAVORITE + "("
            + KeyakiFeedContent.Favorite.Key.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KeyakiFeedContent.Favorite.Key.MEMBER_ID + " INTEGER NOT NULL"
            + ")";

    //@formatter:on
    private static final String DROP_FAVORITE_TABLE_SQL = "DROP TABLE IF EXISTS "
            + KeyakiFeedContent.TABLE_FAVORITE;

    //@formatter:off
    private static final String CREATE_UNREAD_TABLE_SQL = "CREATE TABLE "
            + KeyakiFeedContent.TABLE_UNREAD + "("
            + KeyakiFeedContent.UnRead.Key.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + KeyakiFeedContent.UnRead.Key.ARTICLE_URL + " TEXT NOT NULL,"
            + KeyakiFeedContent.UnRead.Key.MEMBER_ID + " TEXT NOT NULL"
            + ")";

    //@formatter:on
    private static final String DROP_UNREAD_TABLE_SQL = "DROP TABLE IF EXISTS "
            + KeyakiFeedContent.TABLE_UNREAD;

    public KeyakiFeedDatabaseHelper(Context context) {
        super(context, KeyakiFeedContent.DATABASE_NAME, null, KeyakiFeedContent.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // databaseバージョンが上がった場合このメソッドはコールされない
        db.execSQL(CREATE_FAVORITE_TABLE_SQL);
        db.execSQL(CREATE_UNREAD_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
