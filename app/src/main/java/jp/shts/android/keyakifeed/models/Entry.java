package jp.shts.android.keyakifeed.models;

import android.os.Parcel;
import android.os.Parcelable;

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

    Entry(Report report) {
        this.id = report.getId();
        this.title = report.getTitle();
        this.url = report.getUrl();
        this.published = report.getPublished();
        this.imageUrlList = report.getImageUrlArray();
        this.memberName = "Official Report";
    }

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The published
     */
    public String getPublished() {
        return DateUtils.parse(this.published);
    }

    /**
     * @param published The published
     */
    public void setPublished(String published) {
        this.published = published;
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

    /**
     * @param imageUrlList The image_url_list
     */
    public void setImageUrlList(String imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    /**
     * @return The memberId
     */
    public Integer getMemberId() {
        return memberId;
    }

    /**
     * @param memberId The member_id
     */
    public void setMemberId(Integer memberId) {
        this.memberId = memberId;
    }

    /**
     * @return The memberName
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * @param memberName The member_name
     */
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    /**
     * @return The memberImageUrl
     */
    public String getMemberImageUrl() {
        return memberImageUrl;
    }

    /**
     * @param memberImageUrl The member_image_url
     */
    public void setMemberImageUrl(String memberImageUrl) {
        this.memberImageUrl = memberImageUrl;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", published='" + published + '\'' +
                ", imageUrlList='" + imageUrlList + '\'' +
                ", memberId=" + memberId +
                ", memberName='" + memberName + '\'' +
                ", memberImageUrl='" + memberImageUrl + '\'' +
                '}';
    }

    protected Entry(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        title = in.readString();
        url = in.readString();
        published = in.readString();
        imageUrlList = in.readString();
        memberId = in.readByte() == 0x00 ? null : in.readInt();
        memberName = in.readString();
        memberImageUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(id);
        }
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(published);
        dest.writeString(imageUrlList);
        if (memberId == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(memberId);
        }
        dest.writeString(memberName);
        dest.writeString(memberImageUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Entry> CREATOR = new Parcelable.Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };
}