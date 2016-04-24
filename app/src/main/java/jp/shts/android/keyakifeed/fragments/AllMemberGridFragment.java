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

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.ArrayRecyclerAdapter;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentAllMemberGridBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMemberBinding;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.models2.Members;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

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
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate()");
        listenerType = getArguments().getString("listenerType");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "onDestroy()");
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView()");

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_member_grid, container, false);

        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMembers();
            }
        });
        binding.refresh.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        binding.refresh.post(new Runnable() {
            @Override
            public void run() {
                getMembers();
                binding.refresh.setRefreshing(true);
            }
        });

        adapter = new AllMemberGridListAdapter(getContext());
        adapter.setOnMemberClickListener(new AllMemberGridListAdapter.OnMemberClickListener() {
            @Override
            public void onClick(Member member) {
                if (listenerType.equals(ListenerType.MEMBER_CHOOSER.name())) {
                    Favorite.toggle(member);
                    adapter.notifyDataSetChanged();

                } else {
                    Intent intent = MemberDetailActivity.getStartIntent(
                            getContext(), member);
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

        return binding.getRoot();
    }

    private void getMembers() {
        Log.v(TAG, "getMembers() start !");
        subscriptions.add(KeyakiFeedApiClient.getAllMembers()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Func1<Throwable, Members>() {
                    @Override
                    public Members call(Throwable throwable) {
                        return new Members();
                    }
                })
                .subscribe(new Action1<Members>() {
                    @Override
                    public void call(Members members) {
                        Log.v(TAG, "getMembers() : onNext() ");
                        if (binding.refresh != null) {
                            if (binding.refresh.isRefreshing()) {
                                binding.refresh.setRefreshing(false);
                            }
                        }
                        if (members == null || members.isEmpty()) {
                            Log.e(TAG, "cannot get members");
                        } else {
                            adapter.reset(members);
                        }
                    }
                }));
    }

//    @Subscribe
//    public void onChangedFavoriteState(Favorite.ChangedFavoriteState state) {
//        if (state.e == null) {
//            adapter.notifyDataSetChanged();
//        }
//    }

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
