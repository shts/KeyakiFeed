package jp.shts.android.keyakifeed.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.utils.PicassoHelper;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();
    private static final int REQUEST_DOWNLOAD = 1;

    public static Intent getStartIntent(Context context, ArrayList<String> imageUrlList, int index) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putStringArrayListExtra("imageUrlList", imageUrlList);
        intent.putExtra("index", index);
        return intent;
    }

    private String downloadTarget;
    private CoordinatorLayout coordinatorLayout;
    private Uri recentDownloadedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);

        final List<String> imageUrlList = getIntent().getStringArrayListExtra("imageUrlList");
        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return imageUrlList.size();
            }
            @Override
            public boolean isViewFromObject(View view, Object object) {
                return object.equals(view);
            }
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                final ImageView iv = new ImageView(getApplicationContext());
                final String url = imageUrlList.get(position);
                iv.setTag(url);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                PicassoHelper.load(iv, url);
                iv.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        DownloadConfirmDialog confirmDialog = new DownloadConfirmDialog();
                        confirmDialog.setCallbacks(new DownloadConfirmDialog.Callbacks() {
                            @Override
                            public void onClickPositiveButton() {
                                download((String) v.getTag());
                            }
                            @Override
                            public void onClickNegativeButton() {}
                        });
                        confirmDialog.show(getSupportFragmentManager(), TAG);
                        return false;
                    }
                });
                container.addView(iv);
                return iv;
            }
            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                View v = (View) object;
                container.removeView(v);
            }
        });
        viewPager.setCurrentItem(getIntent().getIntExtra("index", 0));
        BusHolder.get().register(this);
    }

    @Override
    protected void onDestroy() {
        BusHolder.get().unregister(this);
        super.onDestroy();
    }

    private void download(String url) {
        if (!hasPermission()) {
            // 権限がない場合はリクエスト
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DOWNLOAD);
            downloadTarget = url;
            return;
        }
        if (!new SimpleImageDownloader(getApplicationContext(), url).get()) {
            Snackbar.make(coordinatorLayout, "ダウンロードに失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean hasPermission() {
        final int permission = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_DOWNLOAD == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download(downloadTarget);
            } else {
                Snackbar.make(coordinatorLayout, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    /**
     * SimpleImageDownloader のコールバック
     * @param callback callback
     */
    @Subscribe
    public void onFinishDownload(SimpleImageDownloader.Callback callback) {
        if (callback == null || callback.file == null) {
            Snackbar.make(coordinatorLayout, "ダウンロードに失敗しました", Snackbar.LENGTH_LONG).show();
            return;
        }
        SdCardUtils.scanFile(this, callback.file,
                new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                Log.w(TAG, "path(" + path + ") uri(" + uri + ")");
                recentDownloadedUri = uri;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(coordinatorLayout, "ダウンロード完了しました", Snackbar.LENGTH_LONG)
                                .setAction("確認する", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.setDataAndType(recentDownloadedUri, "image/jpeg");
                                        startActivity(intent);
                                    }
                                })
                                .setActionTextColor(ContextCompat.getColor(
                                        getApplicationContext(), R.color.accent))
                                .show();
                    }
                });
            }
        });
    }

}
