package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.utils.DateUtils;
import jp.shts.android.keyakifeed.utils.PicassoHelper;

public class AllFeedListAdapter extends BindableAdapter<Entry> {

    private static final String TAG = AllFeedListAdapter.class.getSimpleName();

    private OnPageMaxScrolledListener pageMaxScrolledListener;

    public interface OnPageMaxScrolledListener {
        public void onScrolledMaxPage();
    }

    public void setPageMaxScrolledListener(OnPageMaxScrolledListener listener) {
        pageMaxScrolledListener = listener;
    }

    public class ViewHolder {
        ImageView profileImageView;
        ImageView favoriteImageView;
        TextView titleTextView;
        TextView authorNameTextView;
        TextView updatedTextView;

        public ViewHolder(View view) {
            profileImageView = (ImageView) view.findViewById(R.id.profile_image);
            favoriteImageView = (ImageView) view.findViewById(R.id.favorite_icon);
            titleTextView = (TextView) view.findViewById(R.id.title);
            authorNameTextView = (TextView) view.findViewById(R.id.authorname);
            updatedTextView = (TextView) view.findViewById(R.id.updated);
        }
    }

    public AllFeedListAdapter(Context context, List<Entry> list) {
        super(context, list);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View view = inflater.inflate(R.layout.list_item_entry, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(final Entry entry, int position, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        final String profileImageUrl = entry.getAuthorImageUrl();
        if (profileImageUrl == null) {
            // kenkyusei
            holder.profileImageView.setImageResource(R.drawable.ic_account_circle_black_48dp);
        } else {
            PicassoHelper.loadAndCircleTransform(getContext(), holder.profileImageView, profileImageUrl);
        }
        holder.profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Context context = getContext();
                Intent intent = MemberDetailActivity.getStartIntent(context, entry.getAuthorId());
                context.startActivity(intent);
            }
        });

        holder.titleTextView.setText(entry.getTitle());
        holder.authorNameTextView.setText(entry.getAuthor());
        holder.updatedTextView.setText(DateUtils.dateToString(entry.getPublishedDate()));
        boolean isFavorite =
                Favorite.exist(entry.getAuthorId());
        holder.favoriteImageView.setVisibility(isFavorite ? View.VISIBLE : View.GONE);

        if (getCount() - 1 <= position) {
            if (pageMaxScrolledListener != null) {
                pageMaxScrolledListener.onScrolledMaxPage();
            }
        }
    }
}
