package jp.shts.android.keyakifeed.providers.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;

import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.providers.KeyakiFeedContent;

public class Favorites extends ArrayList<Favorite> {

    private static final String TAG = Favorites.class.getSimpleName();

    @NonNull
    public static Favorites all(@NonNull Context context) {
        Favorites favorites = new Favorites();
        Cursor c = context.getContentResolver().query(
                KeyakiFeedContent.Favorite.CONTENT_URI, null, null, null, null);
        if (c == null || !c.moveToFirst()) return favorites;
        try {
            do {
                int id = c.getInt(c.getColumnIndex(KeyakiFeedContent.Favorite.Key.ID));
                int memberId = c.getInt(c.getColumnIndex(KeyakiFeedContent.Favorite.Key.MEMBER_ID));
                favorites.add(new Favorite(id, memberId));
            } while (c.moveToNext());
        } finally {
            c.close();
        }
        return favorites;
    }

    /**
     * 指定したメンバーのお気に入り状況をトグルする
     * @param context
     * @param member
     * @return トグルした結果を返却する。trueなら推しメン登録, falseなら推しメン解除
     */
    public static void toggle(@NonNull Context context, @NonNull Member member) {
        if (exist(context, member)) {
            remove(context, member);
        } else {
            add(context, member);
        }
    }

    private static void add(@NonNull Context context, @NonNull Member member) {
        ContentValues cv = new ContentValues();
        cv.put(KeyakiFeedContent.Favorite.Key.MEMBER_ID, member.getId());
        context.getContentResolver().insert(KeyakiFeedContent.Favorite.CONTENT_URI, cv);
    }

    private static void remove(@NonNull Context context, @NonNull Member member) {
        String selection = KeyakiFeedContent.Favorite.Key.MEMBER_ID + "=?";
        String[] selectionArgs = { String.valueOf(member.getId()) };
        context.getContentResolver().delete(KeyakiFeedContent.Favorite.CONTENT_URI, selection, selectionArgs);
    }

    public static boolean exist(@NonNull Context context, @NonNull Member member) {
        String selection = KeyakiFeedContent.Favorite.Key.MEMBER_ID + "=?";
        String[] selectionArgs = { String.valueOf(member.getId()) };

        Cursor c = context.getContentResolver().query(
                KeyakiFeedContent.Favorite.CONTENT_URI,
                KeyakiFeedContent.Favorite.sProjection,
                selection, selectionArgs, null);
        if (c == null || !c.moveToFirst()) return false;
        try {
            return c.getCount() == 1;
        } finally {
            c.close();
        }
    }

    // TODO: 後で消す
    public static boolean exist(@NonNull Context context, @NonNull String memberId) {
        String selection = KeyakiFeedContent.Favorite.Key.MEMBER_ID + "=?";
        String[] selectionArgs = { memberId };

        Cursor c = context.getContentResolver().query(
                KeyakiFeedContent.Favorite.CONTENT_URI,
                KeyakiFeedContent.Favorite.sProjection,
                selection, selectionArgs, null);
        if (c == null || !c.moveToFirst()) return false;
        try {
            return c.getCount() == 1;
        } finally {
            c.close();
        }
    }

    public boolean contain(int memberId) {
        for (Favorite f : this) {
            if(f.memberId == memberId) return true;
        }
        return false;
    }

    @NonNull
    public ArrayList<Integer> getMemberIdList() {
        ArrayList<Integer> list = new ArrayList<>();
        for (Favorite f : this) {
            list.add(f.memberId);
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Favorites {").append("\n");
        for (Favorite f : this) {
            sb.append(f.toString()).append("\n");
        }
        sb.append("}").append("\n");
        return sb.toString();
    }
}
