package jp.shts.android.keyakifeed.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.parse.GetCallback;
import com.parse.ParseException;

import java.io.File;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.utils.DateUtils;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;

public class BlogFragment extends Fragment {

    private static final String TAG = BlogFragment.class.getSimpleName();

    public static BlogFragment newBlogFragment(String entryObjectId) {
        Bundle bundle = new Bundle();
        bundle.putString("entryObjectId", entryObjectId);
        BlogFragment blogFragment = new BlogFragment();
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
    private FABProgressCircle fabProgressCircle;
    private CoordinatorLayout coordinatorLayout;

    private Uri recentDownloadedUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryObjectId = getArguments().getString("entryObjectId");
    }

    @SuppressLint("AddJavascriptInterface")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_blog, null);

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
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
        fabProgressCircle = (FABProgressCircle) view.findViewById(R.id.progress);
        fabProgressCircle.attachListener(new FABProgressListener() {
            @Override
            public void onFABProgressAnimationEnd() {
                showSnackbar();
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<String> urls = entry.getImageUrlList();
                if (urls == null || urls.isEmpty()) {
                    Toast.makeText(getActivity(),
                            R.string.toast_failed_download, Toast.LENGTH_SHORT).show();
                    return;
                }
                fabProgressCircle.show();
                download(urls);
            }
        });

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
                    if (!download(url)) {
                        Toast.makeText(getActivity(),
                                R.string.toast_failed_download, Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onClickNegativeButton() {}
            });
            confirmDialog.show(getFragmentManager(), TAG);
        }
    }

    private boolean download(String url) {
//        return new SimpleImageDownloader(getActivity(), url) {
//            @Override
//            public void onResponse(File file) {
//                super.onResponse(file);
//                SdCardUtils.scanFile(getActivity(),
//                        file, new MediaScannerConnection.OnScanCompletedListener() {
//                            @Override
//                            public void onScanCompleted(String path, Uri uri) {
//                                Log.w(TAG, "path(" + path + ") uri(" + uri + ")");
//                                recentDownloadedUri = uri;
//                                showSnackbar();
//                            }
//                        });
//            }
//        }.get();
        return true;
    }

    private boolean download(List<String> urls) {
//        return new SimpleImageDownloader(getActivity(), urls) {
//            @Override
//            public void onResponse(File file) {
//                super.onResponse(file);
//                SdCardUtils.scanFile(getActivity(),
//                        file, new MediaScannerConnection.OnScanCompletedListener() {
//                            @Override
//                            public void onScanCompleted(String path, Uri uri) {
//                                Log.w(TAG, "path(" + path + ") uri(" + uri + ")");
//                                recentDownloadedUri = uri;
//                            }
//                        });
//            }
//            @Override
//            public void onComplete(List<Response> responseList) {
//                fabProgressCircle.beginFinalAnimation();
//            }
//        }.get();
        return true;
    }

    private void showSnackbar() {
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
    }

}
