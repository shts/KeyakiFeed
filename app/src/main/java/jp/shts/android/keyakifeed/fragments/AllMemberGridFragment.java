package jp.shts.android.keyakifeed.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.ArrayRecyclerAdapter;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentAllMemberGridBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMemberBinding;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.Members;
import jp.shts.android.keyakifeed.providers.FavoriteContentObserver;
import jp.shts.android.keyakifeed.providers.dao.Favorites;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AllMemberGridFragment extends Fragment {

    private static final String TAG = AllMemberGridFragment.class.getSimpleName();

    @StringDef({ListenerType.MEMBER_CHOOSER, ListenerType.START_DETAIL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ListenerType {
        String MEMBER_CHOOSER = "member_chooser";
        String START_DETAIL = "start_detail";
    }

    public static AllMemberGridFragment newInstance(@ListenerType String listenerType) {
        Bundle bundle = new Bundle();
        bundle.putString("listenerType", listenerType);
        AllMemberGridFragment allMemberGridFragment = new AllMemberGridFragment();
        allMemberGridFragment.setArguments(bundle);
        return allMemberGridFragment;
    }

    private AllMemberGridListAdapter adapter;
    private String listenerType;
    private FragmentAllMemberGridBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    private final FavoriteContentObserver favoriteContentObserver = new FavoriteContentObserver() {
        @Override
        public void onChangeState(@State int state) {
            if (adapter != null) adapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listenerType = getArguments().getString("listenerType");
        // 推しメン登録時には個別にnotifyするためメンバー一覧画面のみ登録する
        if (ListenerType.START_DETAIL.equals(listenerType))
            favoriteContentObserver.register(getContext());
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        if (ListenerType.START_DETAIL.equals(listenerType))
            favoriteContentObserver.unregister(getContext());
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_member_grid, container, false);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMembers();
            }
        });

        adapter = new AllMemberGridListAdapter(getContext());
        adapter.setOnMemberClickListener(new AllMemberGridListAdapter.OnMemberClickListener() {
            @Override
            public void onClick(Member member, int position) {
                if (listenerType.equals(ListenerType.MEMBER_CHOOSER)) {
                    Favorites.toggle(getContext(), member);
                    adapter.notifyItemChanged(position);
                    getActivity().setResult(Activity.RESULT_OK);

                } else {
                    Intent intent = MemberDetailActivity.getStartIntent(
                            getContext(), member);
                    getContext().startActivity(intent);
                }
            }
        });
        binding.recyclerview.setAdapter(adapter);

        if (listenerType.equals(ListenerType.MEMBER_CHOOSER)) {
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
        } else if (listenerType.equals(ListenerType.START_DETAIL)) {
            binding.toolbar.setVisibility(View.GONE);
        }

        getMembers();

        return binding.getRoot();
    }

    private void getMembers() {
        subscriptions.add(KeyakiFeedApiClient.getAllMembers()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        binding.refresh.setRefreshing(true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Members>() {
                    @Override
                    public void onCompleted() {
                        binding.refresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        // TODO: error handling
                        binding.refresh.setRefreshing(false);
                    }

                    @Override
                    public void onNext(Members members) {
                        if (members != null && !members.isEmpty()) {
                            adapter.reset(members);
                        }
                    }
                }));
    }

    private static class AllMemberGridListAdapter extends ArrayRecyclerAdapter<Member, BindingHolder<ListItemMemberBinding>> {

        private static final String TAG = AllMemberGridListAdapter.class.getSimpleName();

        AllMemberGridListAdapter(Context context) {
            super(context);
        }

        private OnMemberClickListener listener;

        interface OnMemberClickListener {
            void onClick(Member member, int position);
        }

        void setOnMemberClickListener(OnMemberClickListener listener) {
            this.listener = listener;
        }

        @Override
        public BindingHolder<ListItemMemberBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BindingHolder<>(getContext(), parent, R.layout.list_item_member);
        }

        @Override
        public void onBindViewHolder(final BindingHolder<ListItemMemberBinding> holder, int position) {
            ListItemMemberBinding binding = holder.binding;
            final Member member = getItem(position);
            binding.setMember(member);
            binding.profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) listener.onClick(member, holder.getAdapterPosition());
                }
            });
        }
    }
}
