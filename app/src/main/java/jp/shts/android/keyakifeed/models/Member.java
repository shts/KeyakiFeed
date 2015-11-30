package jp.shts.android.keyakifeed.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

@ParseClassName("Member")
public class Member extends ParseObject {

    private static final String TAG = Member.class.getSimpleName();

    public static ParseQuery<Member> getQuery() {
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        return query;
    }

    public String getNameMain() {
        return getString("name_main");
    }

    public String getNameSub() {
        return getString("name_sub");
    }

    public String getProfileImageUrl() {
        return getString("image_url");
    }

    public String getHeight() {
        return getString("height");
    }

    public String getConstellation() {
        return getString("constellation");
    }

    public String getBloodType() {
        return getString("bloodtype");
    }

    public String getBirthday() {
        return getString("birthday");
    }

    public String getBirthPlace() {
        return getString("birthplace");
    }
}
