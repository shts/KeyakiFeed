package jp.shts.android.keyakifeed.entities;

import android.os.Parcel;
import android.os.Parcelable;

import jp.shts.android.keyakifeed.models.Entry;

public class BlogImage implements Parcelable {

    private String imageUrl;
    private String thumbnailUrl;
    private int entryId;
    private String title;
    private String published;

    public BlogImage(String imageUrl, String thumbnailUrl, Entry entry) {
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.entryId = entry.getId();
        this.title = entry.getTitle();
        this.published = entry.getPublished();
    }

    protected BlogImage(Parcel in) {
        imageUrl = in.readString();
        thumbnailUrl = in.readString();
        entryId = in.readInt();
        title = in.readString();
        published = in.readString();
    }

    public static final Creator<BlogImage> CREATOR = new Creator<BlogImage>() {
        @Override
        public BlogImage createFromParcel(Parcel in) {
            return new BlogImage(in);
        }

        @Override
        public BlogImage[] newArray(int size) {
            return new BlogImage[size];
        }
    };

    public String getImageUrl() {
        return imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getEntryId() {
        return entryId;
    }

    public String getTitle() {
        return title;
    }

    public String getPublished() {
        return published;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageUrl);
        dest.writeString(thumbnailUrl);
        dest.writeInt(entryId);
        dest.writeString(title);
        dest.writeString(published);
    }
}