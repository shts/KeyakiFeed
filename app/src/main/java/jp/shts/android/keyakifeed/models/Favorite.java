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
import jp.shts.android.keyakifeed.models2.Member;

@ParseClassName("Favorite")
public class Favorite extends ParseObject {

    private static final String TAG = Favorite.class.getSimpleName();

    private static ParseQuery<Favorite> getQuery(Member member) {
        ParseQuery<Favorite> query = ParseQuery.getQuery(Favorite.class);
        query.whereEqualTo("memberId", member.getId());
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

    public static boolean exist(Member member) {
        try {
            List<Favorite> favoriteList = getQuery(member).find();
            return (favoriteList != null && !favoriteList.isEmpty());
        } catch (ParseException e) {
            Log.e(TAG, "cannot exit", e);
        }
        return false;
    }

    public static void toggle(Member member) {
        if (!exist(member)) {
            add(member);
        } else {
            delete(member);
        }
    }

    public static void add(Member member) {
        Favorite favorite = new Favorite();
        UUID uuid = UUID.randomUUID();
        favorite.setUuid(uuid.toString());
        favorite.setMemberObjectId(member.getId());
        favorite.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                BusHolder.get().post(new ChangedFavoriteState(ChangedFavoriteState.Action.ADD, e));
            }
        });
    }

    public static void delete(Member member) {
        try {
            Favorite favorite = getQuery(member).getFirst();
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

    public void setMemberObjectId(int memberId) {
        put("memberId", memberId);
    }

    public int getMemberObjectId() {
        return getInt("memberId");
    }
}
