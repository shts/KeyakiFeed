package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.ActivityGalleryBinding;
import jp.shts.android.keyakifeed.entities.BlogImage;
import jp.shts.android.keyakifeed.fragments.GalleryFragment;
import jp.shts.android.keyakifeed.models.eventbus.RxBusProvider;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class GalleryActivity extends AppCompatActivity {

    public static Intent getStartIntent(Context context,
                                        ArrayList<BlogImage> blogImages,
                                        int index) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putParcelableArrayListExtra("blogImages", blogImages);
        intent.putExtra("index", index);
        return intent;
    }

    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGalleryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery);
        List<BlogImage> blogImages = getIntent().getParcelableArrayListExtra("blogImages");
        binding.viewPager.setAdapter(new GalleryPagerAdapter(blogImages));
        binding.viewPager.setCurrentItem(getIntent().getIntExtra("index", 0));

        binding.toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // noinspection ConstantConditions
            findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        subscriptions.add(getDownloadSubscription());
    }

    @Override
    protected void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    private Subscription getDownloadSubscription() {
        return RxBusProvider.getInstance()
                .toObservable()
                .observeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(GalleryActivity.this, "ダウンロードに失敗しました", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Object o) {
                        if (o instanceof SimpleImageDownloader.Callback) {
                            SimpleImageDownloader.Callback callback
                                    = (SimpleImageDownloader.Callback) o;
                            if (callback.file == null) {
                                Toast.makeText(GalleryActivity.this, "ダウンロードに失敗しました", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            SdCardUtils.scanFile(GalleryActivity.this, callback.file,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                            GalleryActivity.this.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(GalleryActivity.this, "ダウンロード完了しました", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GalleryPagerAdapter extends FragmentStatePagerAdapter {

        private final List<BlogImage> blogImageList;

        GalleryPagerAdapter(List<BlogImage> blogImageList) {
            super(GalleryActivity.this.getSupportFragmentManager());
            this.blogImageList = blogImageList;
        }

        @Override
        public Fragment getItem(int position) {
            return GalleryFragment.newInstance(blogImageList.get(position));
        }

        @Override
        public int getCount() {
            return blogImageList.size();
        }
    }

}
