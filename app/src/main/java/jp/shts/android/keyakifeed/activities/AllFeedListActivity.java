package jp.shts.android.keyakifeed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.AllFeedListFragment;

public class AllFeedListActivity extends AppCompatActivity {

    private static final String TAG = AllFeedListActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AllFeedListFragment allFeedListFragment = new AllFeedListFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, allFeedListFragment, AllFeedListFragment.class.getSimpleName());
        ft.commit();
    }
}
