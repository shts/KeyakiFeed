package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.AllMemberGridFragment;
import jp.shts.android.keyakifeed.fragments.AllMemberGridFragment.ListenerType;

public class AllMemberActivity extends AppCompatActivity {

    private static final String TAG = AllMemberActivity.class.getSimpleName();

    public static Intent getChooserIntent(Context context) {
        return new Intent(context, AllMemberActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AllMemberGridFragment allMemberGridListFragment
                = AllMemberGridFragment.newInstance(ListenerType.MEMBER_CHOOSER);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, allMemberGridListFragment, AllMemberActivity.class.getSimpleName());
        ft.commit();
    }
}
