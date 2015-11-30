package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.utils.DateUtils;

public class MemberDetailFeedListAdapter extends BindableAdapter<Entry> {

    private static final String TAG = MemberDetailFeedListAdapter.class.getSimpleName();

    private OnPageMaxScrolledListener pageMaxScrolledListener;

    public MemberDetailFeedListAdapter(Context context, List<Entry> list) {
        super(context, list);
    }

    public interface OnPageMaxScrolledListener {
        public void onScrolledMaxPage();
    }

    public void setPageMaxScrolledListener(OnPageMaxScrolledListener listener) {
        pageMaxScrolledListener = listener;
    }

    public class ViewHolder {
        TextView titleTextView;
        TextView authorNameTextView;
        TextView updatedTextView;

        public ViewHolder(View view) {
            titleTextView = (TextView) view.findViewById(R.id.title);
            authorNameTextView = (TextView) view.findViewById(R.id.authorname);
            updatedTextView = (TextView) view.findViewById(R.id.updated);
        }
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        View view = inflater.inflate(R.layout.list_item_member_detail_entry, container, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(Entry entry, int position, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.titleTextView.setText(entry.getTitle());
        holder.authorNameTextView.setText(entry.getAuthor());
        holder.updatedTextView.setText(DateUtils.dateToString(entry.getPublishedDate()));

        if (getCount() - 1 <= position) {
            if (pageMaxScrolledListener != null) {
                pageMaxScrolledListener.onScrolledMaxPage();
            }
        }
    }
}
