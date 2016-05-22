package jp.shts.android.keyakifeed.providers;


import android.net.Uri;
import android.provider.BaseColumns;

public class KeyakiFeedContent {

    private static final String TAG = KeyakiFeedContent.class.getSimpleName();

    public static final String AUTHORITY = "jp.shts.android.keyakifeed.providers.keyakifeed";
    public static final String DATABASE_NAME = "keyakifeed.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_FAVORITE = "favorite";
    public static final String TABLE_UNREAD = "unread";

    public static final class Favorite implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorite");
        public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/favorite/filter");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.keyakifeed.favorite";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.keyakifeed.favorite";

        public @interface Key {
            String ID = BaseColumns._ID;
            String MEMBER_ID = "member_id";
        }

        public static final String[] sProjection = {
                Key.ID, Key.MEMBER_ID
        };
    }

    public static final class UnRead implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/unread");
        public static final Uri CONTENT_FILTER_URI = Uri.parse("content://" + AUTHORITY + "/unread/filter");

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.keyakifeed.unread";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.keyakifeed.unread";

        public @interface Key {
            String ID = BaseColumns._ID;
            String MEMBER_ID = "member_id";
            String ARTICLE_URL = "article_url";
        }

        public static final String[] sProjection = {
                Key.ID, Key.MEMBER_ID, Key.ARTICLE_URL
        };
    }
}
