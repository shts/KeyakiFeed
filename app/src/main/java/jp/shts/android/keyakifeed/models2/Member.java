package jp.shts.android.keyakifeed.models2;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Member implements Parcelable {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name_main")
    @Expose
    private String nameMain;
    @SerializedName("name_sub")
    @Expose
    private String nameSub;
    @SerializedName("image_url")
    @Expose
    private String imageUrl;
    @SerializedName("birthday")
    @Expose
    private String birthday;
    @SerializedName("birthplace")
    @Expose
    private String birthplace;
    @SerializedName("blood_type")
    @Expose
    private String bloodType;
    @SerializedName("constellation")
    @Expose
    private String constellation;
    @SerializedName("height")
    @Expose
    private String height;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("favorite")
    @Expose
    private Integer favorite;
    @SerializedName("key")
    @Expose
    private String key;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message_url")
    @Expose
    private String messageUrl;

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
     * @return The nameMain
     */
    public String getNameMain() {
        return nameMain;
    }

    /**
     * @param nameMain The name_main
     */
    public void setNameMain(String nameMain) {
        this.nameMain = nameMain;
    }

    /**
     * @return The nameSub
     */
    public String getNameSub() {
        return nameSub;
    }

    /**
     * @param nameSub The name_sub
     */
    public void setNameSub(String nameSub) {
        this.nameSub = nameSub;
    }

    /**
     * @return The imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @param imageUrl The image_url
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @return The birthday
     */
    public String getBirthday() {
        return birthday;
    }

    /**
     * @param birthday The birthday
     */
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    /**
     * @return The birthplace
     */
    public String getBirthplace() {
        return birthplace;
    }

    /**
     * @param birthplace The birthplace
     */
    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    /**
     * @return The bloodType
     */
    public String getBloodType() {
        return bloodType;
    }

    /**
     * @param bloodType The blood_type
     */
    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    /**
     * @return The constellation
     */
    public String getConstellation() {
        return constellation;
    }

    /**
     * @param constellation The constellation
     */
    public void setConstellation(String constellation) {
        this.constellation = constellation;
    }

    /**
     * @return The height
     */
    public String getHeight() {
        return height;
    }

    /**
     * @param height The height
     */
    public void setHeight(String height) {
        this.height = height;
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
     * @return The favorite
     */
    public Integer getFavorite() {
        return favorite;
    }

    /**
     * @param favorite The favorite
     */
    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    /**
     * @return The key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key The key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return The messageUrl
     */
    public String getMessageUrl() {
        return messageUrl;
    }

    /**
     * @param messageUrl The message_url
     */
    public void setMessageUrl(String messageUrl) {
        this.messageUrl = messageUrl;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("member { ");
        sb.append("id(" + id + ") ");
        sb.append("nameMain(" + nameMain + ") ");
        sb.append("nameSub(" + nameSub + ") ");
        sb.append("imageUrl(" + imageUrl + ") ");
        sb.append("birthday(" + birthday + ") ");
        sb.append("birthplace(" + birthplace + ") ");
        sb.append("bloodType(" + bloodType + ") ");
        sb.append("constellation(" + constellation + ") ");
        sb.append("height(" + height + ") ");
        sb.append("createdAt(" + createdAt + ") ");
        sb.append("updatedAt(" + updatedAt + ") ");
        sb.append("favorite(" + favorite + ") ");
        sb.append("key(" + key + ") ");
        sb.append("status(" + status + ") ");
        sb.append("messageUrl(" + messageUrl + ") ");
        sb.append("}");
        return sb.toString();
    }

    protected Member(Parcel in) {
        id = in.readByte() == 0x00 ? null : in.readInt();
        nameMain = in.readString();
        nameSub = in.readString();
        imageUrl = in.readString();
        birthday = in.readString();
        birthplace = in.readString();
        bloodType = in.readString();
        constellation = in.readString();
        height = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        favorite = in.readByte() == 0x00 ? null : in.readInt();
        key = in.readString();
        status = in.readString();
        messageUrl = in.readString();
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
        dest.writeString(nameMain);
        dest.writeString(nameSub);
        dest.writeString(imageUrl);
        dest.writeString(birthday);
        dest.writeString(birthplace);
        dest.writeString(bloodType);
        dest.writeString(constellation);
        dest.writeString(height);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        if (favorite == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(favorite);
        }
        dest.writeString(key);
        dest.writeString(status);
        dest.writeString(messageUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Member> CREATOR = new Parcelable.Creator<Member>() {
        @Override
        public Member createFromParcel(Parcel in) {
            return new Member(in);
        }

        @Override
        public Member[] newArray(int size) {
            return new Member[size];
        }
    };
}