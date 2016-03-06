package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.databinding.ListItemMemberDetailEntryBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;

public class MemberFeedListAdapter extends FooterRecyclerViewAdapter<Entry, ListItemMemberDetailEntryBinding> {

    private static final String TAG = MemberFeedListAdapter.class.getSimpleName();

    public MemberFeedListAdapter(Context context, List<Entry> list) {
        super(context, list);
    }

    @Override
    public BindingHolder<ListItemMemberDetailEntryBinding> onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new BindingHolder<>(getContext(), parent, R.layout.list_item_member_detail_entry);
    }

    @Override
    public void onBindContentItemViewHolder(BindingHolder<ListItemMemberDetailEntryBinding> bindingHolder, final Entry entry) {
        ListItemMemberDetailEntryBinding detailEntryBinding = bindingHolder.binding;
        detailEntryBinding.setEntry(entry);

        final View root = detailEntryBinding.getRoot();
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(
                        BlogActivity.getStartIntent(getContext(), new Blog(entry)));
            }
        });
    }

}
