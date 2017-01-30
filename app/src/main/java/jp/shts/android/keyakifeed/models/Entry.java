package jp.shts.android.keyakifeed.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import jp.shts.android.keyakifeed.utils.DateUtils;

public class Entry implements Parcelable {

    @SerializedName(value = "__id", alternate = {"id", "_id"})
    @Expose
    private Integer id;
    @SerializedName(value = "__title", alternate = {"title", "_title"})
    @Expose
    private String title;
    @SerializedName(value = "__url", alternate = {"url", "_url"})
    @Expose
    private String url;
    @SerializedName(value = "__published", alternate = {"published", "_published"})
    @Expose
    private String published;
    @SerializedName(value = "__image_url_list", alternate = {"image_url_list", "_image_url_list"})
    @Expose
    private String imageUrlList;
    @SerializedName(value = "__member_id", alternate = {"member_id", "_member_id"})
    @Expose
    private Integer memberId;
    @SerializedName(value = "__member_name", alternate = {"member_name", "_member_name"})
    @Expose
    private String memberName;
    @SerializedName(value = "__member_image_url", alternate = {"member_image_url", "_member_image_url"})
    @Expose
    private String memberImageUrl;
    @Nullable
    @SerializedName(value = "__thumbnail_url_list", alternate = {"thumbnail_url_list", "_thumbnail_url_list"})
    @Expose
    private String thumbnailUrlList;

    Entry(Report report) {
        this.id = report.getId();
        this.title = report.getTitle();
        this.url = report.getUrl();
        this.published = report.getPublished();
        this.imageUrlList = report.getImageUrlArray();
        this.memberName = "Official Report";
    }

    protected Entry(Parcel in) {
        title = in.readString();
        url = in.readString();
        published = in.readString();
        imageUrlList = in.readString();
        memberName = in.readString();
        memberImageUrl = in.readString();
        thumbnailUrlList = in.readString();
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public Integer getMemberId() {
        return memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getMemberImageUrl() {
        return memberImageUrl;
    }

    /**
     * @return The published
     */
    public String getPublished() {
        return DateUtils.parse(this.published);
    }

    /**
     * @return The imageUrlList
     */
    public ArrayList<String> getImageUrlList() {
        ArrayList<String> list = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(imageUrlList);
            final int N = array.length();
            for (int i = 0; i < N; i++) {
                list.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Nullable
    public ArrayList<String> getThumbnailUrlList() {
        if (thumbnailUrlList != null) {
            ArrayList<String> list = new ArrayList<>();
            try {
                JSONArray array = new JSONArray(thumbnailUrlList);
                final int N = array.length();
                for (int i = 0; i < N; i++) {
                    list.add(array.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return list;
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(published);
        dest.writeString(imageUrlList);
        dest.writeString(memberName);
        dest.writeString(memberImageUrl);
        dest.writeString(thumbnailUrlList);
    }
}