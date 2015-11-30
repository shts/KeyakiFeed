package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.BlogFragment;

public class BlogActivity extends AppCompatActivity {

    private static final String TAG = BlogActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, String entryObjectId) {
        Intent intent = new Intent(context, BlogActivity.class);
        intent.putExtra("entryObjectId", entryObjectId);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BlogFragment blogFragment = BlogFragment.newBlogFragment(
                getIntent().getStringExtra("entryObjectId"));

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, blogFragment, BlogFragment.class.getSimpleName());
        ft.commit();
    }
}
