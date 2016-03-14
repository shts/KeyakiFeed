package jp.shts.android.keyakifeed.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

@ParseClassName("Entry")
public class Entry extends ParseObject {

    private static final String TAG = Entry.class.getSimpleName();

    public static ParseQuery<Entry> getQuery(int limit, int skip) {
        ParseQuery<Entry> query = ParseQuery.getQuery(Entry.class);
        query.orderByDescending("published");
        query.setLimit(limit);
        query.setSkip(skip);
        return query;
    }

    public static void all(ParseQuery<Entry> query) {
        query.findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                BusHolder.get().post(new GetEntriesCallback.All(entries, e));
            }
        });
    }

    public static void next(ParseQuery<Entry> query) {
        query.findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                BusHolder.get().post(new GetEntriesCallback.Next(entries, e));
            }
        });
    }

    public static void findByIdAll(ParseQuery<Entry> query, String memberObjecctId) {
        query.whereEqualTo("member_id", memberObjecctId);
        query.findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                BusHolder.get().post(new GetEntriesCallback.FindById.All(entries, e));
            }
        });
    }

    public static void findByIdNext(ParseQuery<Entry> query, String memberObjectId) {
        query.whereEqualTo("member_id", memberObjectId);
        query.findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                BusHolder.get().post(new GetEntriesCallback.FindById.Next(entries, e));
            }
        });
    }

    public static void memberAllImages(String memberObjectId) {
        ParseQuery<Entry> query = ParseQuery.getQuery(Entry.class);
        query.orderByDescending("published");
        query.findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                BusHolder.get().post(new GetEntriesCallback.AllImage(entries, e));
            }
        });
    }

    /**
     * For event bus callbacks
     */
    public static class GetEntriesCallback {
        final public List<Entry> entries;
        final public ParseException e;
        GetEntriesCallback(List<Entry> entries, ParseException e) {
            this.entries = entries;
            this.e = e;
        }
        public static class All extends GetEntriesCallback {
            All(List<Entry> entries, ParseException e) {
                super(entries, e);
            }
        }
        public static class Next extends GetEntriesCallback {
            Next(List<Entry> entries, ParseException e) {
                super(entries, e);
            }
        }
        public static class FindById extends GetEntriesCallback {
            FindById(List<Entry> entries, ParseException e) {
                super(entries, e);
            }
            public static class All extends FindById {
                All(List<Entry> entries, ParseException e) {
                    super(entries, e);
                }
            }
            public static class Next extends FindById {
                Next(List<Entry> entries, ParseException e) {
                    super(entries, e);
                }
            }
        }
        public static class AllImage extends GetEntriesCallback {
            AllImage(List<Entry> entries, ParseException e) {
                super(entries, e);
            }
        }
        public boolean hasError() { return e != null || (entries == null || entries.isEmpty()); }

        public List<String> getAllThumbnailUrlList() {
            List<String> list = new ArrayList<>();
            if (hasError()) return list;

            for (Entry entry : entries) {
                list.addAll(entry.getImageUrlList());
            }
            return list;
        }
    }

    public boolean isFavorite() {
        return Favorite.exist(getMemberId());
    }

    public String getArticleUrl() {
        return getString("article_url");
    }

    public String getMemberName() {
        return getString("member_name");
    }

    public String getMemberId() {
        return getString("member_id");
    }

    public String getMemberImageUrl() {
        return getString("member_image_url");
    }

    public List<String> getImageUrlList() {
        return getList("image_url_list");
    }

    public Date getPublishedDate() {
        return getDate("published");
    }

    public String getTitle() {
        return getString("title");
    }

}
