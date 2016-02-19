package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.utils.DateUtils;

public class MemberFeedListAdapter2 extends FooterRecyclerViewAdapter<Entry> {

    private static final String TAG = MemberFeedListAdapter2.class.getSimpleName();

    class FooterHolder extends RecyclerView.ViewHolder {
        View root;
        public FooterHolder(View view) {
            super(view);
            root = view;
        }
        void setVisibility(boolean visibility) {
            if (visibility) {
                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView authorNameTextView;
        TextView updatedTextView;
        View root;

        public ViewHolder(View view) {
            super(view);
            root = view;
            titleTextView = (TextView) view.findViewById(R.id.title);
            authorNameTextView = (TextView) view.findViewById(R.id.authorname);
            updatedTextView = (TextView) view.findViewById(R.id.updated);
        }
    }

    private FooterHolder footerViewHolder;

    private OnItemClickCallback clickCallback;
    private OnMaxPageScrolledListener listener;

    public MemberFeedListAdapter2(Context context, List<Entry> list) {
        super(context, list);
    }

    @Override
    public RecyclerView.ViewHolder onCreateFooterItemViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.list_item_more_load, viewGroup, false);
        return new FooterHolder(view);
    }

    @Override
    public RecyclerView.ViewHolder onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.list_item_member_detail_entry, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindFooterItemViewHolder(RecyclerView.ViewHolder viewHolder) {
        footerViewHolder = (FooterHolder) viewHolder;
    }

    @Override
    public void onBindContentItemViewHolder(RecyclerView.ViewHolder viewHolder, final Entry entry) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        holder.titleTextView.setText(entry.getTitle());
        holder.authorNameTextView.setText(entry.getAuthor());
        holder.updatedTextView.setText(DateUtils.dateToString(entry.getPublishedDate()));
        holder.root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clickCallback != null) clickCallback.onClick(entry);
            }
        });
    }

    @Override
    protected void onMaxPageScrolled() {
        if (listener != null) listener.onMaxPageScrolled();
    }

    /**
     * Listener for list item clicked.
     */
    public interface OnItemClickCallback {
        public void onClick(Entry entry);
    }

    /**
     * Listener for list max scrolled.
     */
    public interface OnMaxPageScrolledListener {
        public void onMaxPageScrolled();
    }

    public void setClickCallback(OnItemClickCallback clickCallback) {
        this.clickCallback = clickCallback;
    }

    public void setOnMaxPageScrolled(OnMaxPageScrolledListener listener) {
        this.listener = listener;
    }

    public void setVisibility(boolean visibility) {
        footerViewHolder.setVisibility(visibility);
    }

}
