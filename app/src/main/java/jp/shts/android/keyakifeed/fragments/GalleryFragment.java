package jp.shts.android.keyakifeed.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.GalleryActivity;
import jp.shts.android.keyakifeed.databinding.FragmentGalleryBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.entities.BlogImage;
import jp.shts.android.keyakifeed.utils.PicassoHelper;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;

/**
 */
public class GalleryFragment extends Fragment {

    private static final String TAG = GalleryFragment.class.getSimpleName();

    private FragmentGalleryBinding binding;
    private String downloadTarget;
    private Uri recentDownloadedUri;

    @NonNull
    public static GalleryFragment newInstance(BlogImage blogImage) {
        GalleryFragment galleryFragment = new GalleryFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("blogImage", blogImage);
        galleryFragment.setArguments(bundle);
        return galleryFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_gallery, container, false);
        final BlogImage blogImage = getArguments().getParcelable("blogImage");
        binding.title.setText(blogImage.entry.getTitle());
        binding.date.setText(blogImage.entry.getPublished());
        PicassoHelper.load(binding.image, blogImage.url);
        binding.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DownloadConfirmDialog confirmDialog = new DownloadConfirmDialog();
                confirmDialog.setCallbacks(new DownloadConfirmDialog.Callbacks() {
                    @Override
                    public void onClickPositiveButton() {
                        //download((String) v.getTag());
                    }
                    @Override
                    public void onClickNegativeButton() {}
                });
                confirmDialog.show(getActivity().getSupportFragmentManager(), TAG);
                return false;
            }
        });
        binding.footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(BlogActivity.getStartIntent(
                        getActivity(), new Blog(blogImage.entry)));
            }
        });
        return binding.getRoot();
    }

    private void download(String url) {
        if (!hasPermission()) {
            // 権限がない場合はリクエスト
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            downloadTarget = url;
            return;
        }
        if (!new SimpleImageDownloader(getContext(), url).get()) {
            Snackbar.make(binding.coordinator, "ダウンロードに失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }

    private boolean hasPermission() {
        final int permission = ContextCompat.checkSelfPermission(
                getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (0 == requestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                download(downloadTarget);
            } else {
                Snackbar.make(binding.coordinator, "アプリに書き込み権限がないためダウンロードできません。", Snackbar.LENGTH_LONG)
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
            Snackbar.make(binding.coordinator, "ダウンロードに失敗しました", Snackbar.LENGTH_LONG).show();
            return;
        }
        SdCardUtils.scanFile(getContext(), callback.file,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.w(TAG, "path(" + path + ") uri(" + uri + ")");
                        recentDownloadedUri = uri;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(binding.coordinator, "ダウンロード完了しました", Snackbar.LENGTH_LONG)
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
                                                getActivity(), R.color.accent))
                                        .show();
                            }
                        });
                    }
                });
    }

}
