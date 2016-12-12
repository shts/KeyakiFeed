package jp.shts.android.keyakifeed.entities;

import android.os.Parcel;
import android.os.Parcelable;

import jp.shts.android.keyakifeed.models.Entry;

public class BlogImage implements Parcelable {

    public String url;
    public Entry entry;

    public BlogImage(String imageUrl, Entry entry) {
        this.url = imageUrl;
        this.entry = entry;
    }

    protected BlogImage(Parcel in) {
        url = in.readString();
        entry = (Entry) in.readValue(Entry.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeValue(entry);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<BlogImage> CREATOR = new Parcelable.Creator<BlogImage>() {
        @Override
        public BlogImage createFromParcel(Parcel in) {
            return new BlogImage(in);
        }

        @Override
        public BlogImage[] newArray(int size) {
            return new BlogImage[size];
        }
    };

}