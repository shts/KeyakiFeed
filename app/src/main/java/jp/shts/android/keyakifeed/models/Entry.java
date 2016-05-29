package jp.shts.android.keyakifeed.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Entry implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("published")
    @Expose
    private String published;
    @SerializedName("image_url_list")
    @Expose
    private String imageUrlList;
    @SerializedName("member_id")
    @Expose
    private Integer memberId;
    @SerializedName("member_name")
    @Expose
    private String memberName;
    @SerializedName("member_image_url")
    @Expose
    private String memberImageUrl;

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
        return published;
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
        StringBuilder sb = new StringBuilder();
        sb.append("entry {").append("\n");
        sb.append("id(").append(id).append(") ");
        sb.append("title(").append(title).append(") ");
        sb.append("url(").append(url).append(") ");
        sb.append("published(").append(published).append(") ");
        sb.append("imageUrlList(").append(imageUrlList).append(") ");
        sb.append("memberId(").append(memberId).append(") ");
        sb.append("memberImageUrl(").append(memberImageUrl).append(") ");
        sb.append("memberName(").append(memberName).append(") ");
        sb.append("}");
        return sb.toString();
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