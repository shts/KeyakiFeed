package jp.shts.android.keyakifeed.models;

import java.util.ArrayList;

public class Members extends ArrayList<Member> {

    private static final String TAG = Members.class.getSimpleName();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append("Members").append("\n");
        for (Member member : this) {
            sb.append(member.toString()).append("\n");
        }
        return sb.toString();
    }
}
