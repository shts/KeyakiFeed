package jp.shts.android.keyakifeed.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.FragmentBlogBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.ShareUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;
import jp.shts.android.keyakifeed.utils.WaitMinimunImageDownloader;

public class BlogFragment extends Fragment {

    private static final String TAG = BlogFragment.class.getSimpleName();

    public static BlogFragment newInstance(Blog blog) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("blog", blog);
        BlogFragment blogFragment = new BlogFragment();
        blogFragment.setArguments(bundle);
        return blogFragment;
    }

    private FragmentBlogBinding binding;
    private Uri recentDownloadedUri;

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHolder.get().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_blog, container, false);

        final Blog blog = getArguments().getParcelable("blog");
        if (blog == null) {
            return binding.getRoot();
        }

        binding.toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        binding.fabActionShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.multipleActions.collapse();
                getActivity().startActivity(ShareUtils.getShareBlogIntent(blog));
            }
        });
        binding.fabActionDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> urlList = blog.getImageUrlList();
                if (urlList == null || urlList.isEmpty()) {
                    Snackbar.make(binding.coordinator, "ダウンロードする画像がありません", Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    binding.fabActionDownload.setColorNormalResId(R.color.primary);
                    binding.fabActionDownload.setTitle("ダウンロード中です ...");
                    download(urlList);
                }
            }
        });

        binding.browser.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView webView = (WebView) v;
                showDownloadConfirmDialog(webView);
                return false;
            }
        });
        binding.browser.getSettings().setJavaScriptEnabled(true);

        binding.toolbar.setTitle(blog.getTitle());
        binding.toolbar.setSubtitle(blog.getMemberName());

        binding.browser.loadUrl(blog.getUrl());

        return binding.getRoot();
    }

    private void showDownloadConfirmDialog(WebView webView) {
        WebView.HitTestResult hr = webView.getHitTestResult();
        if ((WebView.HitTestResult.IMAGE_TYPE == hr.getType())
                || (WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE == hr.getType())) {
            final String url = hr.getExtra();
            DownloadConfirmDialog confirmDialog = new DownloadConfirmDialog();
            confirmDialog.setCallbacks(new DownloadConfirmDialog.Callbacks() {
                @Override
                public void onClickPositiveButton() {
                    download(url);
                }
                @Override
                public void onClickNegativeButton() {}
            });
            confirmDialog.show(getFragmentManager(), TAG);
        }
    }

    private List<String> downloadTargetList;
    private void download(List<String> urlList) {
        if (!hasPermission()) {
            // 権限がない場合はリクエスト
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DOWNLOAD_ALL);
            downloadTargetList = urlList;
            return;
        }
        if (!new WaitMinimunImageDownloader(getActivity(), urlList).get()) {
            showSnackbar(false);
        }
    }

    private String downloadTarget;
    private void download(String url) {
        if (!hasPermission()) {
            // 権限がない場合はリクエスト
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_DOWNLOAD);
            downloadTarget = url;
            return;
        }
        if (!new SimpleImageDownloader(getActivity(), url).get()) {
            showSnackbar(false);
        }
    }

    private static final int REQUEST_DOWNLOAD = 1;
    private static final int REQUEST_DOWNLOAD_ALL = 2;

    private boolean hasPermission() {
        final int permission = ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (REQUEST_DOWNLOAD == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download(downloadTarget);
            } else {
                binding.fabActionDownload.setColorNormalResId(R.color.accent);
                binding.fabActionDownload.setTitle("画像をダウンロードする");
                Snackbar.make(binding.coordinator, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
                        .show();
            }
        } else if (REQUEST_DOWNLOAD_ALL == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download(downloadTargetList);
            } else {
                binding.fabActionDownload.setColorNormalResId(R.color.accent);
                binding.fabActionDownload.setTitle("画像をダウンロードする");
                Snackbar.make(binding.coordinator, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
                        .show();
            }
        }
    }

    /**
     * WaitMinimunImageDownloader のコールバック
     * @param callback callback
     */
    @Subscribe
    public void onFinishDownload(
            WaitMinimunImageDownloader.Callback.ResponseDownloadImage callback) {
        if (callback != null && callback.file != null) {
            SdCardUtils.scanFile(getActivity(), callback.file,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.w(TAG, "path(" + path + ") uri(" + uri + ")");
                            recentDownloadedUri = uri;
                        }
                    });
        }
    }

    /**
     * WaitMinimunImageDownloader のコールバック
     * @param callback callback
     */
    @Subscribe
    public void onFinishDownload(
            WaitMinimunImageDownloader.Callback.CompleteDownloadImage callback) {
        // TODO: レスポンスリスト内にerrorがないことを確認してからSnackbarを表示すること
        // TODO: 一部画像のダウンロードに失敗した場合はその旨を通知すること
        showSnackbar(true);
    }

    /**
     * SimpleImageDownloader のコールバック
     * @param callback callback
     */
    @Subscribe
    public void onFinishDownload(SimpleImageDownloader.Callback callback) {
        if (callback != null && callback.file != null) {
            SdCardUtils.scanFile(getActivity(), callback.file,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.w(TAG, "path(" + path + ") uri(" + uri + ")");
                            recentDownloadedUri = uri;
                            final Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showSnackbar(true);
                                    }
                                });
                            }
                        }
                    });
        }
    }

    private void showSnackbar(final boolean isSucceed) {
        binding.fabActionDownload.setColorNormalResId(R.color.accent);
        binding.fabActionDownload.setTitle("画像をダウンロードする");
        if (isSucceed) {
            Snackbar.make(binding.coordinator, "ダウンロード完了しました", Snackbar.LENGTH_LONG)
                    .setAction("確認する", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(recentDownloadedUri, "image/jpeg");
                            getActivity().startActivity(intent);
                        }
                    })
                    .setActionTextColor(getResources().getColor(R.color.accent))
                    .show();
        } else {
            Snackbar.make(binding.coordinator, "ダウンロードに失敗しました。通信環境をご確認下さい", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

}
