package jp.shts.android.keyakifeed.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Matome implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("feed_title")
    @Expose
    private String feedTitle;
    @SerializedName("feed_url")
    @Expose
    private String feedUrl;
    @SerializedName("entry_title")
    @Expose
    private String entryTitle;
    @SerializedName("entry_url")
    @Expose
    private String entryUrl;
    @SerializedName("entry_published")
    @Expose
    private String entryPublished;
    @SerializedName("entry_categories")
    @Expose
    private String entryCategories;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    /**
     *
     * @return
     * The id
     */
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The feedTitle
     */
    public String getFeedTitle() {
        return feedTitle;
    }

    /**
     *
     * @param feedTitle
     * The feed_title
     */
    public void setFeedTitle(String feedTitle) {
        this.feedTitle = feedTitle;
    }

    /**
     *
     * @return
     * The feedUrl
     */
    public String getFeedUrl() {
        return feedUrl;
    }

    /**
     *
     * @param feedUrl
     * The feed_url
     */
    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    /**
     *
     * @return
     * The entryTitle
     */
    public String getEntryTitle() {
        return entryTitle;
    }

    /**
     *
     * @param entryTitle
     * The entry_title
     */
    public void setEntryTitle(String entryTitle) {
        this.entryTitle = entryTitle;
    }

    /**
     *
     * @return
     * The entryUrl
     */
    public String getEntryUrl() {
        return entryUrl;
    }

    /**
     *
     * @param entryUrl
     * The entry_url
     */
    public void setEntryUrl(String entryUrl) {
        this.entryUrl = entryUrl;
    }

    /**
     *
     * @return
     * The entryPublished
     */
    public String getEntryPublished() {
        return entryPublished;
    }

    /**
     *
     * @param entryPublished
     * The entry_published
     */
    public void setEntryPublished(String entryPublished) {
        this.entryPublished = entryPublished;
    }

    /**
     *
     * @return
     * The entryCategories
     */
    public String getEntryCategories() {
        return entryCategories;
    }

    /**
     *
     * @param entryCategories
     * The entry_categories
     */
    public void setEntryCategories(String entryCategories) {
        this.entryCategories = entryCategories;
    }

    /**
     *
     * @return
     * The createdAt
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     *
     * @param createdAt
     * The created_at
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     *
     * @return
     * The updatedAt
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     *
     * @param updatedAt
     * The updated_at
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }


    protected Matome(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        feedTitle = in.readString();
        feedUrl = in.readString();
        entryTitle = in.readString();
        entryUrl = in.readString();
        entryPublished = in.readString();
        entryCategories = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
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
        dest.writeString(feedTitle);
        dest.writeString(feedUrl);
        dest.writeString(entryTitle);
        dest.writeString(entryUrl);
        dest.writeString(entryPublished);
        dest.writeString(entryCategories);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Matome> CREATOR = new Parcelable.Creator<Matome>() {
        @Override
        public Matome createFromParcel(Parcel in) {
            return new Matome(in);
        }

        @Override
        public Matome[] newArray(int size) {
            return new Matome[size];
        }
    };
}