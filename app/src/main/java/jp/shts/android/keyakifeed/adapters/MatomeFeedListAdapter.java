package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.utils.PicassoHelper;

public class MatomeFeedListAdapter extends BindableAdapter<FeedItem> {

    private static final String TAG = MatomeFeedListAdapter.class.getSimpleName();

    class ViewHolder {

        TextView titleTextView;
        TextView siteTextView;
        TextView dateTextView;
        ImageView thumbnailImageView;

        ViewHolder(View view) {
            titleTextView = (TextView) view.findViewById(R.id.title);
            siteTextView = (TextView) view.findViewById(R.id.site);
            dateTextView = (TextView) view.findViewById(R.id.date);
            thumbnailImageView = (ImageView) view.findViewById(R.id.thumbnail);
        }
    }

    public MatomeFeedListAdapter(Context context, List<FeedItem> list) {
        super(context, list);
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View view = inflater.inflate(R.layout.list_item_matome_feed, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(FeedItem item, int position, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.titleTextView.setText(item.title);
        holder.siteTextView.setText(item.siteTitle);
        holder.dateTextView.setText(item.getFormatedDateText());
        final String thumbnail = item.getThumbnailUrl();
        if (TextUtils.isEmpty(thumbnail)) {
            holder.thumbnailImageView.setVisibility(View.GONE);
        } else {
            holder.thumbnailImageView.setVisibility(View.VISIBLE);
            PicassoHelper.load(getContext(), holder.thumbnailImageView, thumbnail);
        }
    }
}
