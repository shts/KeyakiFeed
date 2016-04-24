package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.MemberDetailFragment;
import jp.shts.android.keyakifeed.models2.Member;

public class MemberDetailActivity extends AppCompatActivity {

    private static final String TAG = MemberDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, Member member) {
        Intent intent = new Intent(context, MemberDetailActivity.class);
        intent.putExtra("member", member);
        return intent;
    }
    public static Intent getStartIntent(Context context, int memberId) {
        Intent intent = new Intent(context, MemberDetailActivity.class);
        intent.putExtra("memberId", memberId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Member member = getIntent().getParcelableExtra("member");
        MemberDetailFragment memberDetailFragment;
        if (member != null) {
            memberDetailFragment
                    = MemberDetailFragment.newInstance(member);
        } else {
            memberDetailFragment
                    = MemberDetailFragment.newInstance(getIntent().getIntExtra("memberId", 0));
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, memberDetailFragment, MemberDetailFragment.class.getSimpleName());
        ft.commit();
    }

}
