package jp.shts.android.keyakifeed.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.AllMemberActivity;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentFavoriteFeedListBinding;
import jp.shts.android.keyakifeed.databinding.ListItemCardBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.providers.dao.Favorites;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 推しメンのブログ記事一覧
 */
public class FavoriteMemberFeedListFragment extends Fragment {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();

    private FragmentFavoriteFeedListBinding binding;
    private FavoriteFeedListAdapter adapter;

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_feed_list, container, false);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMemberChooser();
            }
        });
        binding.recyclerview.setHasFixedSize(true); // アイテムは固定サイズ
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setupFavoriteMemberFeed();
            }
        });
        binding.refresh.setSwipeableChildren(R.id.recyclerview, R.id.empty_view);
        binding.refresh.setColorSchemeResources(R.color.primary);
        setupFavoriteMemberFeed();

        return binding.getRoot();
    }

    private void startMemberChooser() {
        Intent intent = AllMemberActivity.getChooserIntent(getContext());
        startActivityForResult(intent, 0);
    }

    private void setupFavoriteMemberFeed() {
        setVisibilityEmptyView(false);

        final Favorites favorites = Favorites.all(getContext());
        if (favorites.isEmpty()) {
            setVisibilityEmptyView(true);
            return;
        }

        Subscription subscription = getEntries(favorites.getMemberIdList())
                .subscribe(new Action1<Entries>() {
                    @Override
                    public void call(Entries entries) {
                        if (entries.isEmpty()) return;
                        adapter = new FavoriteFeedListAdapter(getActivity(), entries);
                        adapter.setOnMaxPageScrollListener(new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
                            @Override
                            public void onMaxPageScrolled() {
                                if (nowGettingNextEntry) return;
                                nowGettingNextEntry = true;

                                Subscription subscription = getNextEntries(favorites.getMemberIdList())
                                        .subscribe(new Action1<Entries>() {
                                            @Override
                                            public void call(Entries entries) {
                                                if (adapter == null) return;
                                                if (entries == null || entries.isEmpty()) {
                                                    adapter.setFoooterVisibility(false);
                                                } else {
                                                    adapter.setFoooterVisibility(true);
                                                    adapter.add(entries);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }
                                        });
                                subscriptions.add(subscription);
                            }
                        });
                        binding.recyclerview.setAdapter(adapter);
                        setVisibilityEmptyView(false);
                    }
                });
        subscriptions.add(subscription);
    }

    private Observable<Entries> getEntries(List<Integer> memberIdList) {
        counter = 0;
        return KeyakiFeedApiClient.getMemberEntries(
                memberIdList, (PAGE_LIMIT * counter), PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (binding.refresh.isRefreshing()) {
                            binding.refresh.setRefreshing(true);
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (binding.refresh.isRefreshing()) {
                            binding.refresh.setRefreshing(false);
                        }
                    }
                })
                .onErrorReturn(new Func1<Throwable, Entries>() {
                    @Override
                    public Entries call(Throwable e) {
                        setVisibilityEmptyView(true);
                        if (binding.refresh != null) {
                            if (binding.refresh.isRefreshing()) {
                                binding.refresh.setRefreshing(false);
                            }
                        }
                        return new Entries();
                    }
                });
    }

    private Observable<Entries> getNextEntries(List<Integer> memberIdList) {
        // get next feed
        counter++;
        return KeyakiFeedApiClient.getMemberEntries(
                memberIdList, (PAGE_LIMIT * counter), PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void setVisibilityEmptyView(boolean isVisible) {
        if (isVisible) {
            // recyclerView を setVisiblity(View.GONE) で表示にするとプログレスが表示されない
            binding.recyclerview.setAdapter(null);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            setupFavoriteMemberFeed();
        }
    }

    public static class FavoriteFeedListAdapter extends FooterRecyclerViewAdapter<Entry, ListItemCardBinding> {

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
}
