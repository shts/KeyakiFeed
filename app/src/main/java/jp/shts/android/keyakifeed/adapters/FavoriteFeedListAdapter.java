package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.databinding.ListItemCardBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;

public class FavoriteFeedListAdapter extends FooterRecyclerViewAdapter<Entry, ListItemCardBinding> {

    private static final String TAG = FavoriteFeedListAdapter.class.getSimpleName();

    public FavoriteFeedListAdapter(Context context, List<Entry> list) {
        super(context, list);
    }

    @Override
    public BindingHolder<ListItemCardBinding> onCreateContentItemViewHolder(
            LayoutInflater inflater, ViewGroup parent) {
        return new BindingHolder<>(getContext(), parent, R.layout.list_item_card);
    }

    @Override
    public void onBindContentItemViewHolder(
            BindingHolder<ListItemCardBinding> bindingHolder, final Entry entry) {
        ListItemCardBinding cardBinding = bindingHolder.binding;
        cardBinding.setEntry(entry);

        cardBinding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(
                        MemberDetailActivity.getStartIntent(getContext(), entry.getMemberId()));
            }
        });

        final View root = cardBinding.getRoot();
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(
                        BlogActivity.getStartIntent(getContext(), new Blog(entry)));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (root.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
                p.setMargins(8, 8, 8, 8);
                root.requestLayout();
            }
        }
    }
}
