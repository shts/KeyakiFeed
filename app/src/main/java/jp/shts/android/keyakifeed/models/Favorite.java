package jp.shts.android.keyakifeed.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@ParseClassName("Favorite")
public class Favorite extends ParseObject {

    private static final String TAG = Favorite.class.getSimpleName();

    private static ParseQuery<Favorite> getQuery(String memberObjectId) {
        ParseQuery<Favorite> query = ParseQuery.getQuery(Favorite.class);
        query.whereEqualTo("memberObjectId", memberObjectId);
        query.fromLocalDatastore();
        return query;
    }

    public static boolean exist(String memberObjectId) {
        try {
            List<Favorite> favoriteList = getQuery(memberObjectId).find();
            return (favoriteList != null && !favoriteList.isEmpty());
        } catch (ParseException e) {
            Log.e(TAG, "cannot exit", e);
        }
        return false;
    }

    public static void toggle(String memberObjectId) {
        Log.v(TAG, "exist(" + exist(memberObjectId) + ")");
        if (!exist(memberObjectId)) {
            add(memberObjectId);
        } else {
            delete(memberObjectId);
        }
    }

    public static void add(String memberObjectId) {
        Log.v(TAG, "add member(" + memberObjectId + ")");
        try {
            Favorite favorite = new Favorite();
            UUID uuid = UUID.randomUUID();
//            favorite.add("uuid", uuid.toString());
//            favorite.add("memberObjectId", memberObjectId);
            favorite.setUuid(uuid.toString());
            favorite.setMemberObjectId(memberObjectId);
            favorite.pin();
        } catch (ParseException e) {
            Log.e(TAG, "cannot pin", e);
        }
    }

    public static void delete(String memberObjectId) {
        Log.v(TAG, "delete member(" + memberObjectId + ")");
        try {
            Favorite favorite = getQuery(memberObjectId).getFirst();
            if (favorite != null) {
                favorite.unpin();
            }
        } catch (ParseException e) {
            Log.e(TAG, "cannot unpin", e);
        }
    }

    public void setUuid(String uuid) {
        put("uuid", uuid);
    }

    public void setMemberObjectId(String memberObjectId) {
        put("memberObjectId", memberObjectId);
    }

    public String getMemberObjectId() {
        return getString("memberObjectId");
    }
}
