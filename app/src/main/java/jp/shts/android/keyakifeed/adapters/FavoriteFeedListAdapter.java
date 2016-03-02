package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.databinding.ListItemCardBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;

public class FavoriteFeedListAdapter extends ArrayRecyclerAdapter<Entry, BindingHolder<ListItemCardBinding>> {

    private static final String TAG = FavoriteFeedListAdapter.class.getSimpleName();

    public FavoriteFeedListAdapter(Context context) {
        super(context);
    }

    @Override
    public BindingHolder<ListItemCardBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder<>(getContext(), parent, R.layout.list_item_card);
    }

    @Override
    public void onBindViewHolder(BindingHolder<ListItemCardBinding> holder, int position) {
        final Entry entry = getItem(position);
        ListItemCardBinding cardBinding = holder.binding;
        cardBinding.setEntry(entry);

        cardBinding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(
                        MemberDetailActivity.getStartIntent(context, entry.getMemberId()));
            }
        });

        final View root = cardBinding.getRoot();
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(
                        BlogActivity.getStartIntent(context, new Blog(entry)));
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
