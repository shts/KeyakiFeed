package jp.shts.android.keyakifeed.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jp.shts.android.keyakifeed.models.Report;
import jp.shts.android.keyakifeed.models.Entry;

/**
 * Blog情報を扱うデータクラス
 * TODO: 削除予定
 */
public class Blog implements Parcelable {

    private static final String TAG = Blog.class.getSimpleName();

    private String entryObjectId;
    private String url;
    private String title;
    private String memberId;
    private String memberName;
    private String memberImageUrl;
    private ArrayList<String> imageUrlList;

    public Blog(JSONObject json) {
        try {
            entryObjectId = json.getString("_entryObjectId");
            url = json.getString("_article_url");
            title = json.getString("_title");
            memberId = json.getString("_member_id");
            memberName = json.getString("_member_name");
            memberImageUrl = json.getString("_member_image_url");

            final JSONArray jsonArray = json.getJSONArray("_image_url_list");
            imageUrlList = new ArrayList<>();
            final int N = jsonArray.length();
            for (int i = 0; i < N; i++) {
                imageUrlList.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Blog(Entry entry) {
        //entryObjectId = entry.getId();
        url = entry.getUrl();
        title = entry.getTitle();
        //memberId = entry.getMemberId();
        memberName = entry.getMemberName();
        memberImageUrl = entry.getMemberImageUrl();
        imageUrlList = (ArrayList<String>) entry.getImageUrlList();
    }

    public Blog(String url, String title, ArrayList<String> imageUrlList) {
        this.url = url;
        this.title = title;
        this.memberName = "OFFICIAL REPORT";
        this.imageUrlList = imageUrlList;
    }

    public Blog(Report report) {
        entryObjectId = String.valueOf(report.getId());
        url = report.getUrl();
        title = report.getTitle();
        memberId = "";
        memberName = "OFFICIAL REPORT";
        memberImageUrl = "";
        imageUrlList = (ArrayList<String>) report.getImageUrlList();
    }

    public String getEntryObjectId() { return entryObjectId; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getMemberImageUrl() { return memberImageUrl; }
    public ArrayList<String> getImageUrlList() { return imageUrlList; }

    protected Blog(Parcel in) {
        url = in.readString();
        title = in.readString();
        memberId = in.readString();
        memberName = in.readString();
        memberImageUrl = in.readString();
        if (in.readByte() == 0x01) {
            imageUrlList = new ArrayList<>();
            in.readList(imageUrlList, String.class.getClassLoader());
        } else {
            imageUrlList = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(memberId);
        dest.writeString(memberName);
        dest.writeString(memberImageUrl);
        if (imageUrlList == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(imageUrlList);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Blog> CREATOR = new Parcelable.Creator<Blog>() {
        @Override
        public Blog createFromParcel(Parcel in) {
            return new Blog(in);
        }

        @Override
        public Blog[] newArray(int size) {
            return new Blog[size];
        }
    };

}
