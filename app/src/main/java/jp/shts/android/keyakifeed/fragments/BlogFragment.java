package jp.shts.android.keyakifeed.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.io.File;
import java.util.ArrayList;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.PermissionRequireActivity;
import jp.shts.android.keyakifeed.databinding.FragmentBlogBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.eventbus.RxBusProvider;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.ShareUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;
import jp.shts.android.keyakifeed.utils.WaitMinimunImageDownloader;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class BlogFragment extends Fragment {

    private static final String TAG = BlogFragment.class.getSimpleName();
    private static final int DOWNLOAD = 0;
    private static final int DOWNLOAD_LIST = 1;

    @NonNull
    public static BlogFragment newInstance(@Nullable Entry entry) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("entry", entry);
        BlogFragment blogFragment = new BlogFragment();
        blogFragment.setArguments(bundle);
        return blogFragment;
    }

    private FragmentBlogBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscriptions.add(RxBusProvider.getInstance()
                .toObservable()
                .observeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {
                        binding.fabActionDownload.setColorNormalResId(R.color.accent);
                        binding.fabActionDownload.setTitle("画像をダウンロードする");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Snackbar.make(binding.coordinator, "ダウンロードに失敗しました",
                                Snackbar.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(Object o) {
                        if (o instanceof SimpleImageDownloader.Callback) {
                            SimpleImageDownloader.Callback callback
                                    = (SimpleImageDownloader.Callback) o;
                            if (callback.file != null) {
                                scanComplete(getActivity(), callback.file);
                            }
                        } else if (o instanceof WaitMinimunImageDownloader.Callback.CompleteDownloadImage) {
                            binding.fabActionDownload.setColorNormalResId(R.color.accent);
                            binding.fabActionDownload.setTitle("画像をダウンロードする");
                            WaitMinimunImageDownloader.Callback.CompleteDownloadImage complete
                                    = (WaitMinimunImageDownloader.Callback.CompleteDownloadImage) o;
                            if (complete.responseList != null && !complete.responseList.isEmpty()) {
                                for (int i = complete.responseList.size(); 0 < i; --i) {
                                    WaitMinimunImageDownloader.Response response = complete.responseList.get(i - 1);
                                    if (response.file != null) {
                                        scanComplete(getActivity(), response.file);
                                        return;
                                    }
                                }
                            }

                        } else if (o instanceof WaitMinimunImageDownloader.Callback.ResponseDownloadImage) {
                            final WaitMinimunImageDownloader.Callback.ResponseDownloadImage response
                                    = (WaitMinimunImageDownloader.Callback.ResponseDownloadImage) o;
                            if (response.file != null) {
                                scanComplete(getActivity(), response.file, false);
                            }

                        }
                    }
                }));
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_blog, container, false);

        final Entry entry = getArguments().getParcelable("entry");
        if (entry == null) {
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
                getActivity().startActivity(ShareUtils.getShareBlogIntent(entry));
            }
        });
        binding.fabActionDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> urlList = entry.getImageUrlList();
                if (urlList == null || urlList.isEmpty()) {
                    Snackbar.make(binding.coordinator, "ダウンロードする画像がありません", Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    binding.fabActionDownload.setColorNormalResId(R.color.primary);
                    binding.fabActionDownload.setTitle("ダウンロード中です ...");

                    startActivityForResult(PermissionRequireActivity
                            .getDownloadStartIntent(getActivity(), urlList), DOWNLOAD_LIST);
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
        binding.toolbar.setTitle(entry.getTitle());
        binding.toolbar.setSubtitle(entry.getMemberName());
        binding.browser.loadUrl(entry.getUrl());
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
                    startActivityForResult(PermissionRequireActivity
                            .getDownloadStartIntent(getActivity(), url), DOWNLOAD);
                }

                @Override
                public void onClickNegativeButton() {
                }
            });
            confirmDialog.show(getFragmentManager(), TAG);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            binding.fabActionDownload.setTitle("画像をダウンロードする");
            Snackbar.make(binding.coordinator, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        switch (requestCode) {
            case DOWNLOAD:
                if (!new SimpleImageDownloader(getActivity(), data.getStringArrayListExtra(
                        PermissionRequireActivity.ExtraKey.DOWNLOAD)).get()) {
                    Snackbar.make(binding.coordinator, "ダウンロードに失敗しました", Snackbar.LENGTH_LONG).show();
                }
                break;
            case DOWNLOAD_LIST:
                if (!new WaitMinimunImageDownloader(getActivity(), data.getStringArrayListExtra(
                        PermissionRequireActivity.ExtraKey.DOWNLOAD)).get()) {
                    Snackbar.make(binding.coordinator, "ダウンロードに失敗しました", Snackbar.LENGTH_LONG).show();
                }
                break;
            default:
                return;
        }
    }

    private void scanComplete(@Nullable Context context, @NonNull File file) {
        scanComplete(context, file, true);
    }

    private void scanComplete(@Nullable Context context, @NonNull File file, final boolean showSnackbar) {
        if (context == null) return;
        SdCardUtils.scanFile(context, file,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, final Uri uri) {
                        new android.os.Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (showSnackbar) showSnackbar(uri);
                            }
                        });
                    }
                });
    }

    private void showSnackbar(final Uri uri) {
        Snackbar.make(binding.coordinator, "ダウンロード完了しました", Snackbar.LENGTH_LONG)
                .setAction("確認する", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "image/jpeg");
                        getActivity().startActivity(intent);
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.accent))
                .show();
    }
}
