package jp.shts.android.keyakifeed.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

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

    public String getAuthor() {
        return getString("author");
    }

    public String getAuthorId() {
        return getString("author_id");
    }

    public String getAuthorImageUrl() {
        return getString("author_image_url");
    }

    public String getBody() {
        return getString("body");
    }

    public String getDay() {
        return getString("day");
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

    public String getWeek() {
        return getString("week");
    }

    public String getYearMonth() {
        return getString("yearmonth");
    }
}
