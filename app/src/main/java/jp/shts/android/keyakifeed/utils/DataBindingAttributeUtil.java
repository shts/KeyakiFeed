package jp.shts.android.keyakifeed.utils;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

public class DataBindingAttributeUtil {

    private static final String TAG = DataBindingAttributeUtil.class.getSimpleName();

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView imageView, String url) {
        PicassoHelper.loadAndCircleTransform(imageView, url);
    }

    @BindingAdapter("thumbnailUrl")
    public static void loadThumbnailImage(ImageView imageView, String url) {
        if (TextUtils.isEmpty(url)) {
            imageView.setVisibility(View.GONE);
        } else {
            imageView.setVisibility(View.VISIBLE);
            PicassoHelper.load(imageView, url);
        }
    }

    @BindingAdapter("entryThumbnailUrl")
    public static void loadEntryThumbnailImage(ImageView imageView, List<String> urlList) {
        if (urlList != null && !urlList.isEmpty()) {
            String thumbnailUrl = urlList.get(0);
            PicassoHelper.load(imageView, thumbnailUrl);
        }
    }

}
