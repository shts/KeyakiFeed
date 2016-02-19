package jp.shts.android.keyakifeed.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FeedItem implements Parcelable {

    private static final String TAG = FeedItem.class.getSimpleName();

    public String siteTitle;
    public String siteDescription;
    public String siteUrl;

    public String title;
    public String url;
    public String date;
    public String subject;
    public String description;
    public String content;

    public FeedItem() {}

    public Date getDate() {
        return formatToDate(date);
    }

    public String getFormatedDateText() {
        return formatToString(date);
    }

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy'/'MM'/'dd HH:mm");

    // nogi
    // 2015-12-30T12:51:09Z
    // mtm
    // 2016-01-05T12:00:41+09:00
    // http://docs.oracle.com/javase/jp/7/api/java/text/SimpleDateFormat.html
    private String formatToString(String source) {
        return FORMATTER.format(formatToDate(source));
    }

    private Date formatToDate(String source) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try {
            return sdf.parse(source);
        } catch (ParseException e) {
            Log.e(TAG, "failed to parse");
        }
        return null;
    }

    public String getThumbnailUrl() {

        String html = content.replace("<![CDATA[", "").replace("]]>", "");
        Document document = Jsoup.parse(html);
        Element body = document.body();
        Elements atags = body.getElementsByTag("a");

        for (Element e : atags) {
            String link = e.attr("href");
            if (link.endsWith(".jpg") || link.endsWith(".jpeg") || link.endsWith(".png")) {
                return link;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("siteTitle(").append(siteTitle).append(")").append("\n");
        sb.append("siteDescription(").append(siteDescription).append(")").append("\n");
        sb.append("siteUrl(").append(siteUrl).append(")").append("\n");
        sb.append("title(").append(title).append(")").append("\n");
        sb.append("description(").append(description).append(")").append("\n");
        sb.append("url(").append(url).append(")").append("\n");
        sb.append("date(").append(date).append(")").append("\n");
        sb.append("subject(").append(subject).append(")").append("\n");
        sb.append("content(").append(content).append(")").append("\n");
        return sb.toString();
    }

    protected FeedItem(Parcel in) {
        siteTitle = in.readString();
        siteDescription = in.readString();
        siteUrl = in.readString();
        title = in.readString();
        url = in.readString();
        date = in.readString();
        subject = in.readString();
        description = in.readString();
        content = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(siteTitle);
        dest.writeString(siteDescription);
        dest.writeString(siteUrl);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(date);
        dest.writeString(subject);
        dest.writeString(description);
        dest.writeString(content);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        @Override
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }

        @Override
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };
}