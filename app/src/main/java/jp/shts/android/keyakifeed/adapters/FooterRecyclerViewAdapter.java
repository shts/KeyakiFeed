package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.ListItemMoreLoadBinding;

public abstract class FooterRecyclerViewAdapter<T, ContentViewBindingHolder extends ViewDataBinding>
        extends HeaderFooterRecyclerViewAdapter<BindingHolder<ContentViewBindingHolder>,
        BindingHolder<?>, BindingHolder<ListItemMoreLoadBinding>> {

    private static final String TAG = FooterRecyclerViewAdapter.class.getSimpleName();

    private LayoutInflater inflater;
    private final List<T> list;
    private final Context context;
    private View footerView;

    public FooterRecyclerViewAdapter(Context context, List<T> list) {
        super();
        this.context = context;
        this.list = list;
        setup(context);
    }

    private void setup(Context context) {
        inflater = LayoutInflater.from(context);
    }

    protected Context getContext() {
        return this.context;
    }

    @Override
    protected final int getHeaderItemCount() {
        return 0;
    }

    @Override
    protected final int getFooterItemCount() {
        return 1;
    }

    @Override
    protected final int getContentItemCount() {
        return list.size();
    }

    @Override
    protected final BindingHolder onCreateHeaderItemViewHolder(ViewGroup parent, int headerViewType) {
        return null;
    }

    @Override
    protected final BindingHolder<ListItemMoreLoadBinding> onCreateFooterItemViewHolder(ViewGroup parent, int footerViewType) {
        return new BindingHolder<>(getContext(), parent, R.layout.list_item_more_load);
    }

    @Override
    protected final BindingHolder<ContentViewBindingHolder> onCreateContentItemViewHolder(ViewGroup parent, int contentViewType) {
        return onCreateContentItemViewHolder(inflater, parent);
    }

    public abstract BindingHolder<ContentViewBindingHolder> onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup parent);

    @Override
    protected final void onBindHeaderItemViewHolder(BindingHolder bindingHolder, int position) {}

    @Override
    protected final void onBindContentItemViewHolder(BindingHolder<ContentViewBindingHolder> bindingHolder, int position) {
        onBindContentItemViewHolder(bindingHolder, list.get(position));
        if (getContentItemCount() - 1 <= position) {
            if (listener != null) listener.onMaxPageScrolled();
        }
    }

    @Override
    protected final void onBindFooterItemViewHolder(BindingHolder<ListItemMoreLoadBinding> bindingHolder, int position) {
        footerView = bindingHolder.binding.getRoot();
    }

    public abstract void onBindContentItemViewHolder(BindingHolder<ContentViewBindingHolder> bindingHolder, T t);

    private OnMaxPageScrollListener listener;

    public interface OnMaxPageScrollListener {
        public void onMaxPageScrolled();
    }

    public void setOnMaxPageScrollListener(OnMaxPageScrollListener listener) {
        this.listener = listener;
    }

    public List<T> getList() {
        return this.list;
    }

    public void add(List<T> list) {
        this.list.addAll(list);
    }

    public int getPosition(T t) {
        final int N = list.size();
        for (int i = 0; 0 < N; i++) {
            if (t == list.get(i)) {
                return i;
            }
        }
        return 0;
    }

    public View getFooterView() {
        return footerView;
    }

    public void setFooterVisibility(boolean visibility) {
        final View view = getFooterView();
        if (view == null) { return; }

        if (visibility) {
            getFooterView().setVisibility(View.VISIBLE);
        } else {
            getFooterView().setVisibility(View.GONE);
        }
    }
}