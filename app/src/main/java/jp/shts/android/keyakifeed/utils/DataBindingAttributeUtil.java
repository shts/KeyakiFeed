package jp.shts.android.keyakifeed.utils;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.entities.BlogImage;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.providers.dao.Favorites;
import jp.shts.android.keyakifeed.providers.dao.UnreadArticles;

import static jp.shts.android.keyakifeed.fragments.SettingsFragment.MARK_UNREAD_ARTICLES;
import static jp.shts.android.keyakifeed.fragments.SettingsFragment.MARK_UNREAD_ARTICLES_ONLY_FAVORITE;

public class DataBindingAttributeUtil {

    private static final String TAG = DataBindingAttributeUtil.class.getSimpleName();

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView imageView, String url) {
        PicassoHelper.loadAndCircleTransform(imageView, url);
    }

    @BindingAdapter("gridThumbnailUrl")
    public static void loadGridThumbnailUrl(ImageView imageView, BlogImage blogImage) {
        String url = (TextUtils.isEmpty(blogImage.getThumbnailUrl()) ?
                blogImage.getImageUrl() : blogImage.getThumbnailUrl());
        Picasso.with(imageView.getContext())
                .load(url)
                .fit()
                .placeholder(R.drawable.place_holder)
                .error(R.drawable.no_image)
                .centerCrop()
                .into(imageView);
    }

    @BindingAdapter("entryThumbnailUrl")
    public static void loadEntryThumbnailImage(ImageView imageView, Entry entry) {
        String imageUrl = null;
        List<String> thumbnailUrlList = entry.getThumbnailUrlList();
        if (thumbnailUrlList != null && !thumbnailUrlList.isEmpty()) {
            imageUrl = thumbnailUrlList.get(0);
        } else {
            List<String> urlList = entry.getImageUrlList();
            if (urlList != null && !urlList.isEmpty()) {
                imageUrl = urlList.get(0);
            }
        }

        if (TextUtils.isEmpty(imageUrl)) {
            imageView.setImageResource(R.drawable.no_image);
        } else {
            Picasso.with(imageView.getContext())
                    .load(imageUrl)
                    .fit()
                    .placeholder(R.drawable.place_holder)
                    .error(R.drawable.no_image)
                    .centerCrop()
                    .into(imageView);
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

    /**
     * すべてのブログページのリストアイテムで使用する
     */
    @BindingAdapter("unreadAllEntry")
    public static void setUnreadIconAllEntry(View view, String url) {
        // すべてのブログ画面で未読表示させない設定の場合はGONE
        boolean isShow = PreferencesUtils.getBoolean(
                view.getContext(), MARK_UNREAD_ARTICLES, true);

        if (isShow && UnreadArticles.exist(view.getContext(), url)) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    /**
     * 推しメンのブログページのリストアイテムで使用する
     */
    @BindingAdapter("unreadFavoriteEntry")
    public static void setUnreadIconFavorite(View view, String url) {
        // 推しメンのブログで未読表示させない設定の場合はGONE
        boolean isShow = PreferencesUtils.getBoolean(
                view.getContext(), MARK_UNREAD_ARTICLES_ONLY_FAVORITE, true);

        if (isShow && UnreadArticles.exist(view.getContext(), url)) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

}
