package jp.shts.android.keyakifeed.utils;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.models2.Entry;
import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.providers.dao.Favorites;

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

    @BindingAdapter("gridThumbnailUrl")
    public static void loadGridThumbnailUrl(ImageView imageView, String url) {
        PicassoHelper.load(imageView, url);
    }

    @BindingAdapter("entryThumbnailUrl")
    public static void loadEntryThumbnailImage(ImageView imageView, List<String> urlList) {
        if (urlList != null && !urlList.isEmpty()) {
            String thumbnailUrl = urlList.get(0);
            PicassoHelper.load(imageView, thumbnailUrl);
        } else {
            imageView.setImageResource(R.drawable.no_image);
        }
    }

    @BindingAdapter("reportThumbnailUrl")
    public static void loadReportThumbnailImage(ImageView imageView, String thumbnailUrl) {
        PicassoHelper.load(imageView, thumbnailUrl);
    }

    @BindingAdapter("favorite")
    public static void setFavoriteIcon(ImageView imageView, Entry entry) {
        if (Favorites.exist(imageView.getContext(), String.valueOf(entry.getMemberId()))) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    @BindingAdapter("favoriteMember")
    public static void setFavoriteIcon(ImageView imageView, Member member) {
        if (Favorites.exist(imageView.getContext(), member)) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

}
