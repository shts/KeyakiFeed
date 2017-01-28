package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.fragments.BlogFragment;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Report;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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

    /**
     * EntryオブジェクトをそのままBundleに渡すとBundleのメモリサイズをオーバーするのでここで取得し直す
     * @param context context
     * @param entryId 記事のID
     * @return BlogActivityへのIntent
     */
    @NonNull
    public static Intent getStartIntent(@NonNull Context context, int entryId) {
        Intent intent = new Intent(context, BlogActivity.class);
        intent.putExtra("entryId", entryId);
        return intent;
    }

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Entry entry = getIntent().getParcelableExtra("entry");
        if (entry != null) {
            setupFragment(entry);
            return;
        }

        Report report = getIntent().getParcelableExtra("report");
        if (report != null) {
            setupFragment(report.toEntry());
            return;
        }

        int entryId = getIntent().getIntExtra("entryId", -1);
        if (entryId != -1) {
            subscriptions.add(KeyakiFeedApiClient.getEntry(entryId)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Entry>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(BlogActivity.this, "ブログ記事が読み込めませんでした", Toast.LENGTH_SHORT).show();
                            BlogActivity.this.finish();
                        }

                        @Override
                        public void onNext(Entry entry) {
                            setupFragment(entry);
                        }
                    }));
        }
    }

    private void setupFragment(Entry entry) {
        BlogFragment blogFragment = BlogFragment.newInstance(entry);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, blogFragment, BlogFragment.class.getSimpleName());
        ft.commit();
    }
}
