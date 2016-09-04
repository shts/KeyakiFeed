package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.BlogFragment;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Report;

public class BlogActivity extends AppCompatActivity {

    private static final String TAG = BlogActivity.class.getSimpleName();

    @NonNull
    public static Intent getStartIntent(@NonNull Context context, Entry entry) {
        Intent intent = new Intent(context, BlogActivity.class);
        intent.putExtra("entry", entry);
        return intent;
    }

    @NonNull
    public static Intent getStartIntent(@NonNull Context context, Report report) {
        Intent intent = new Intent(context, BlogActivity.class);
        intent.putExtra("report", report);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Entry entry = getIntent().getParcelableExtra("entry");
        if (entry == null) {
            Report report = getIntent().getParcelableExtra("report");
            if (report != null) entry = report.toEntry();
        }
        BlogFragment blogFragment = BlogFragment.newInstance(entry);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, blogFragment, BlogFragment.class.getSimpleName());
        ft.commit();
    }
}
