package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.ArrayRecyclerAdapter;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.databinding.FragmentAllMemberGridBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMemberBinding;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class AllMemberGridFragment extends Fragment {

    private static final String TAG = AllMemberGridFragment.class.getSimpleName();

    public enum ListenerType {
        MEMBER_CHOOSER, START_DETAIL
    }

    public static AllMemberGridFragment newInstance(ListenerType listenerType) {
        Bundle bundle = new Bundle();
        bundle.putString("listenerType", listenerType.name());
        AllMemberGridFragment allMemberGridFragment = new AllMemberGridFragment();
        allMemberGridFragment.setArguments(bundle);
        return allMemberGridFragment;
    }

    private AllMemberGridListAdapter adapter;
    private String listenerType;
    private FragmentAllMemberGridBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listenerType = getArguments().getString("listenerType");
        BusHolder.get().register(this);
    }

    @Override
    public void onDestroy() {
        BusHolder.get().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_member_grid, container, false);

        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Member.all(Member.getQuery());
            }
        });
        binding.refresh.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        binding.refresh.post(new Runnable() {
            @Override
            public void run() {
                binding.refresh.setRefreshing(true);
            }
        });

        adapter = new AllMemberGridListAdapter(getContext());
        adapter.setOnMemberClickListener(new AllMemberGridListAdapter.OnMemberClickListener() {
            @Override
            public void onClick(Member member) {
                if (listenerType.equals(ListenerType.MEMBER_CHOOSER.name())) {
                    Favorite.toggle(member.getObjectId());
                    adapter.notifyDataSetChanged();

                } else {
                    Intent intent = MemberDetailActivity.getStartIntent(
                            getContext(), member.getObjectId());
                    getContext().startActivity(intent);
                }
            }
        });
        binding.recyclerview.setAdapter(adapter);

        if (listenerType.equals(ListenerType.MEMBER_CHOOSER.name())) {
            binding.toolbar.setVisibility(View.VISIBLE);
            binding.toolbar.setTitleTextColor(ContextCompat.getColor(getContext(), R.color.primary));
            binding.toolbar.setTitle("推しメンを選択してください");
            binding.toolbar.setNavigationIcon(R.drawable.ic_clear_green_500_18dp);
            binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        } else if (listenerType.equals(ListenerType.START_DETAIL.name())) {
            binding.toolbar.setVisibility(View.GONE);
        }

        Member.all(Member.getQuery());

        return binding.getRoot();
    }

    @Subscribe
    public void onGotMembers(Member.GetMembersCallback callback) {
        if (binding.refresh != null) {
            if (binding.refresh.isRefreshing()) {
                binding.refresh.setRefreshing(false);
            }
        }
        if (callback.e != null || callback.members == null || callback.members.isEmpty()) {
            Log.e(TAG, "cannot get members", callback.e);
        } else {
            adapter.reset(callback.members);
        }
    }

    public static class AllMemberGridListAdapter extends ArrayRecyclerAdapter<Member, BindingHolder<ListItemMemberBinding>> {

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
}
