package jp.shts.android.keyakifeed.models;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

@ParseClassName("Member")
public class Member extends ParseObject {

    private static final String TAG = Member.class.getSimpleName();

    public static ParseQuery<Member> getQuery() {
        ParseQuery<Member> query = ParseQuery.getQuery(Member.class);
        return query;
    }

    public static void all(ParseQuery<Member> query) {
        query.findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> members, ParseException e) {
                BusHolder.get().post(new GetMembersCallback(members, e));
            }
        });
    }

    /**
     * For event bus callbacks
     */
    public static class GetMembersCallback {
        public final List<Member> members;
        public final ParseException e;
        GetMembersCallback(List<Member> members, ParseException e) {
            this.members = members;
            this.e = e;
        }
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
