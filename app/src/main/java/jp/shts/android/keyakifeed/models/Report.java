package jp.shts.android.keyakifeed.models;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

@ParseClassName("Report")
public class Report extends ParseObject {

    private static final String TAG = Report.class.getSimpleName();

    public static ParseQuery<Report> getQuery() {
        ParseQuery<Report> query = ParseQuery.getQuery(Report.class);
        query.orderByDescending("published");
        return query;
    }

    public static void all(ParseQuery<Report> query) {
        query.findInBackground(new FindCallback<Report>() {
            @Override
            public void done(List<Report> reports, ParseException e) {
                BusHolder.get().post(new GetReportsCallback(reports, e));
            }
        });
    }

    /**
     * For event bus callbacks
     */
    public static class GetReportsCallback {
        public final List<Report> reports;
        public final ParseException e;
        GetReportsCallback(List<Report> reports, ParseException e) {
            this.reports = reports;
            this.e = e;
        }
        public boolean hasError() {
            return e != null || reports == null || reports.isEmpty();
        }
    }

    public String getUrl() {
        return getString("url");
    }

    public Date getPublished() {
        return getDate("published");
    }

    public List<String> getImageUrlList() {
        return getList("image_url_list");
    }

    public String getTitle() {
        return getString("title");
    }

    public String getThumbnailUrl() {
        return getString("thumbnail_url");
    }

}
