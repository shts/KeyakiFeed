package jp.shts.android.keyakifeed.models;

import android.util.Log;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.List;
import java.util.UUID;

import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

@ParseClassName("Favorite")
public class Favorite extends ParseObject {

    private static final String TAG = Favorite.class.getSimpleName();

    private static ParseQuery<Favorite> getQuery(String memberObjectId) {
        ParseQuery<Favorite> query = ParseQuery.getQuery(Favorite.class);
        query.whereEqualTo("memberObjectId", memberObjectId);
        query.fromLocalDatastore();
        return query;
    }

    public static void all(ParseQuery<Favorite> query) {
        query.findInBackground(new FindCallback<Favorite>() {
            @Override
            public void done(List<Favorite> favorites, ParseException e) {
                BusHolder.get().post(new GetFavoritesCallback(favorites, e));
            }
        });
    }

    /**
     * For event bus callbacks
     */
    public static class GetFavoritesCallback {
        public final List<Favorite> favorites;
        public final ParseException e;
        GetFavoritesCallback(List<Favorite> favorites, ParseException e) {
            this.favorites = favorites;
            this.e = e;
        }
    }

    /**
     * 推しメン登録状態変更通知
     */
    public static class ChangedFavoriteState {
        public enum Action { ADD, REMOVE }
        public final Action action;
        public final ParseException e;
        public ChangedFavoriteState(Action action, ParseException e) {
            this.action = action;
            this.e = e;
        }
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
        Favorite favorite = new Favorite();
        UUID uuid = UUID.randomUUID();
        favorite.setUuid(uuid.toString());
        favorite.setMemberObjectId(memberObjectId);
        favorite.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                BusHolder.get().post(new ChangedFavoriteState(ChangedFavoriteState.Action.ADD, e));
            }
        });
    }

    public static void delete(String memberObjectId) {
        Log.v(TAG, "delete member(" + memberObjectId + ")");
        try {
            Favorite favorite = getQuery(memberObjectId).getFirst();
            if (favorite != null) {
                favorite.unpinInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        BusHolder.get().post(new ChangedFavoriteState(ChangedFavoriteState.Action.REMOVE, e));
                    }
                });
            }
        } catch (ParseException e) {
            Log.e(TAG, "cannot unpin", e);
            BusHolder.get().post(new ChangedFavoriteState(ChangedFavoriteState.Action.REMOVE, e));
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
