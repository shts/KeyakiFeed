package jp.shts.android.keyakifeed.fragments;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.PermissionRequireActivity;
import jp.shts.android.keyakifeed.databinding.FragmentGalleryBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.entities.BlogImage;
import jp.shts.android.keyakifeed.utils.PicassoHelper;
import jp.shts.android.keyakifeed.utils.SdCardUtils;
import jp.shts.android.keyakifeed.utils.SimpleImageDownloader;

public class GalleryFragment extends Fragment {

    private static final String TAG = GalleryFragment.class.getSimpleName();

    private FragmentGalleryBinding binding;

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
                        if (TextUtils.isEmpty(blogImage.url)) {
                            Toast.makeText(getActivity(), "ダウンロードする画像がありません", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivityForResult(PermissionRequireActivity.getDownloadStartIntent(
                                getActivity(), blogImage.url), 0);
                    }

                    @Override
                    public void onClickNegativeButton() {
                    }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(getActivity(), "アプリに書き込み権限がないためダウンロードできません", Toast.LENGTH_SHORT).show();
            return;
        }
        if (data == null) return;

        List<String> urlList = data.getStringArrayListExtra(
                PermissionRequireActivity.ExtraKey.DOWNLOAD);
        if (urlList == null || urlList.isEmpty()) return;

        String url = urlList.get(0);
        if (TextUtils.isEmpty(url)) return;

        Log.e(TAG, "onActivityResult: ");
        if (!new SimpleImageDownloader(getContext(), url).get()) {
            Toast.makeText(getActivity(), "ダウンロードに失敗しました", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * SimpleImageDownloader のコールバック
     *
     * @param callback callback
     */
    @Subscribe
    public void onFinishDownload(SimpleImageDownloader.Callback callback) {
        Log.e(TAG, "onFinishDownload: ");
        if (callback == null || callback.file == null) {
            Toast.makeText(getActivity(), "ダウンロードに失敗しました", Toast.LENGTH_SHORT).show();
            return;
        }
        SdCardUtils.scanFile(getContext(), callback.file,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        Log.e(TAG, "onScanCompleted: ");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "ダウンロード完了しました", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

}
