package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.MemberDetailFragment;

public class MemberDetailActivity extends AppCompatActivity {

    private static final String TAG = MemberDetailActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, String memberObjectId) {
        Intent intent = new Intent(context, MemberDetailActivity.class);
        intent.putExtra("memberObjectId", memberObjectId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String memberObjectId = getIntent().getStringExtra("memberObjectId");
        MemberDetailFragment memberDetailFragment
                = MemberDetailFragment.newMemberDetailFragment(memberObjectId);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, memberDetailFragment, MemberDetailFragment.class.getSimpleName());
        ft.commit();
    }

}
