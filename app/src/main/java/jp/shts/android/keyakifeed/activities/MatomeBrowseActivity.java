package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.fragments.MatomeBrowseFragment;

public class MatomeBrowseActivity extends AppCompatActivity {

    private static final String TAG = MatomeBrowseActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, FeedItem feedItem) {
        Intent intent = new Intent(context, MatomeBrowseActivity.class);
        intent.putExtra("feedItem", feedItem);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FeedItem feedItem = getIntent().getParcelableExtra("feedItem");
        if (feedItem == null || TextUtils.isEmpty(feedItem.url)) {
            Toast.makeText(getApplicationContext(),
                    "記事の取得に失敗しました", Toast.LENGTH_SHORT).show();
            return;
        }

        MatomeBrowseFragment matomeBrowseFragment
                = MatomeBrowseFragment.newInstance(feedItem);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, matomeBrowseFragment, MatomeBrowseFragment.class.getSimpleName());
        ft.commit();
    }
}
