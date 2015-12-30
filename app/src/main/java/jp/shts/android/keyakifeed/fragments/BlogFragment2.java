package jp.shts.android.keyakifeed.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.squareup.otto.Subscribe;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.utils.DateUtils;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.ShareUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;
import jp.shts.android.keyakifeed.utils.WaitMinimunImageDownloader;

public class BlogFragment2 extends Fragment {

    private static final String TAG = BlogFragment2.class.getSimpleName();

    public static BlogFragment2 newInstance(String entryObjectId) {
        Bundle bundle = new Bundle();
        bundle.putString("entryObjectId", entryObjectId);
        BlogFragment2 blogFragment = new BlogFragment2();
        blogFragment.setArguments(bundle);
        return blogFragment;
    }

    private class JavaScriptInterface {
        @JavascriptInterface
        public String getYearMonth() { return entry.getYearMonth(); }
        @JavascriptInterface
        public String getDay() { return entry.getDay(); }
        @JavascriptInterface
        public String getWeek() { return entry.getWeek(); }
        @JavascriptInterface
        public String getTitle() { return entry.getTitle(); }
        @JavascriptInterface
        public String getAuthor() { return entry.getAuthor(); }
        @JavascriptInterface
        public String getBody() { return entry.getBody(); }
        @JavascriptInterface
        public String getPublishedDate() { return DateUtils.dateToString(entry.getPublishedDate()); }
    }

    private String entryObjectId;
    private Entry entry;
    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton fabDownload;
    private FloatingActionsMenu floatingActionsMenu;

    private Uri recentDownloadedUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryObjectId = getArguments().getString("entryObjectId");
    }

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

    @SuppressLint("AddJavascriptInterface")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_blog2, null);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        floatingActionsMenu = (FloatingActionsMenu) view.findViewById(R.id.multiple_actions);
        FloatingActionButton fabShare = (FloatingActionButton) view.findViewById(R.id.fab_action_share);
        fabShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatingActionsMenu.collapse();
                getActivity().startActivity(ShareUtils.getShareBlogIntent(entry));
            }
        });
        fabDownload = (FloatingActionButton) view.findViewById(R.id.fab_action_download);
        fabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabDownload.setColorNormalResId(R.color.primary);
                fabDownload.setTitle("ダウンロード中です ...");
                download(entry.getImageUrlList());
            }
        });

        final WebView webView = (WebView) view.findViewById(R.id.browser);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView webView = (WebView) v;
                showDownloadConfirmDialog(webView);
                return false;
            }
        });
        webView.getSettings().setJavaScriptEnabled(true);
        JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
        webView.addJavascriptInterface(javaScriptInterface, "JavaScriptInterface");

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator);

        entry = Entry.createWithoutData(Entry.class, entryObjectId);
        entry.fetchIfNeededInBackground(new GetCallback<Entry>() {
            @Override
            public void done(Entry entry, ParseException e) {
                if (e != null || entry == null) {
                    Log.e(TAG, "cannot get entry");
                    return;
                }
                toolbar.setTitle(entry.getTitle());
                webView.loadUrl("file:///android_asset/template.html");
            }
        });
        return view;
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
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_DOWNLOAD_ALL);
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
            requestPermissions(new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_DOWNLOAD);
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
                fabDownload.setColorNormalResId(R.color.accent);
                fabDownload.setTitle("画像をダウンロードする");
                Snackbar.make(coordinatorLayout, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
                        .show();
            }
        } else if (REQUEST_DOWNLOAD_ALL == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download(downloadTargetList);
            } else {
                fabDownload.setColorNormalResId(R.color.accent);
                fabDownload.setTitle("画像をダウンロードする");
                Snackbar.make(coordinatorLayout, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
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
                            showSnackbar(true);
                        }
                    });
        }
    }

    private void showSnackbar(final boolean isSucceed) {
        fabDownload.setColorNormalResId(R.color.accent);
        fabDownload.setTitle("画像をダウンロードする");
        if (isSucceed) {
            Snackbar.make(coordinatorLayout, "ダウンロード完了しました", Snackbar.LENGTH_LONG)
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
            Snackbar.make(coordinatorLayout, "ダウンロードに失敗しました。通信環境をご確認下さい", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

}
