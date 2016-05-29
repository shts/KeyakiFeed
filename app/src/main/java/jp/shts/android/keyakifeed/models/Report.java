package jp.shts.android.keyakifeed.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class Report implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("url")
    @Expose
    private String url;
    @SerializedName("thumbnail_url")
    @Expose
    private String thumbnailUrl;
    @SerializedName("published")
    @Expose
    private String published;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("image_url_list")
    @Expose
    private String imageUrlList;

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
     * @return The thumbnailUrl
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * @param thumbnailUrl The thumbnail_url
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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
     * @return The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt The updated_at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id(").append(id).append(") ");
        sb.append("title(").append(title).append(") ");
        sb.append("url(").append(url).append(") ");
        sb.append("thumbnailUrl(").append(thumbnailUrl).append(") ");
        sb.append("published(").append(published).append(") ");
        sb.append("imageUrlList(").append(imageUrlList).append(") ");
        return sb.toString();
    }

    protected Report(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        title = in.readString();
        url = in.readString();
        thumbnailUrl = in.readString();
        published = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        imageUrlList = in.readString();
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
        dest.writeString(thumbnailUrl);
        dest.writeString(published);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeString(imageUrlList);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Report> CREATOR = new Parcelable.Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };
}