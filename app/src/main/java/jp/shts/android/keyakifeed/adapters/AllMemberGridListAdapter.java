package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.ListItemMemberBinding;
import jp.shts.android.keyakifeed.models.Member;

public class AllMemberGridListAdapter extends ArrayRecyclerAdapter<Member, BindingHolder<ListItemMemberBinding>> {

    private static final String TAG = AllMemberGridListAdapter.class.getSimpleName();

    public AllMemberGridListAdapter(Context context) {
        super(context);
    }

    private OnMemberClickListener listener;

    public interface OnMemberClickListener {
        void onClick(Member member);
    }

    public void setOnMemberClickListener(OnMemberClickListener listener) {
        this.listener = listener;
    }

    @Override
    public BindingHolder<ListItemMemberBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder<>(getContext(), parent, R.layout.list_item_member);
    }

    @Override
    public void onBindViewHolder(BindingHolder<ListItemMemberBinding> holder, int position) {
        ListItemMemberBinding binding = holder.binding;
        final Member member = getItem(position);
        binding.setMember(member);
        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onClick(member);
            }
        });
    }
}
