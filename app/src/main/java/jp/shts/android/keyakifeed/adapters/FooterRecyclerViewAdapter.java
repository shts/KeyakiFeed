package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

abstract class FooterRecyclerViewAdapter<T> extends HeaderFooterRecyclerViewAdapter {

    private static final String TAG = FooterRecyclerViewAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    private List<T> list;

    public FooterRecyclerViewAdapter(Context context, List<T> list) {
        super();
        this.list = list;
        setup(context);
    }

    private void setup(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    protected int getHeaderItemCount() {
        return 0;
    }

    @Override
    protected int getFooterItemCount() {
        return 1;
    }

    @Override
    protected int getContentItemCount() {
        return list.size();
    }

    @Override
    protected RecyclerView.ViewHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        return null;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateFooterItemViewHolder(ViewGroup parent, int footerViewType) {
        return onCreateFooterItemViewHolder(inflater, parent);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        return onCreateContentItemViewHolder(inflater, parent);
    }

    public abstract RecyclerView.ViewHolder onCreateFooterItemViewHolder(LayoutInflater inflater, ViewGroup viewGroup);
    public abstract RecyclerView.ViewHolder onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup viewGroup);

    @Override
    protected void onBindHeaderItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {}

    @Override
    protected void onBindFooterItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        onBindFooterItemViewHolder(viewHolder);
    }

    @Override
    protected void onBindContentItemViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        onBindContentItemViewHolder(viewHolder, list.get(position));
        if (getContentItemCount() - 1 <= position) {
            onMaxPageScrolled();
        }
    }

    public abstract void onBindFooterItemViewHolder(RecyclerView.ViewHolder viewHolder);
    public abstract void onBindContentItemViewHolder(RecyclerView.ViewHolder viewHolder, T t);

    /**
     * Notify on max page scrolled.
     */
    protected abstract void onMaxPageScrolled();

    public void add(List<T> list) {
        this.list.addAll(list);
    }
}
