package jp.shts.android.keyakifeed.utils;

import android.databinding.BindingAdapter;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
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
