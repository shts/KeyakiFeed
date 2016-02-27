package jp.shts.android.keyakifeed.utils;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

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

}
