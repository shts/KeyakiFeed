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
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.providers.dao.Favorites;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * 推しメンのブログ記事一覧
 */
public class FavoriteMemberFeedListFragment extends Fragment {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();
    private static final int REQUEST_MEMBER_CHOOSER = 0;

    private FragmentFavoriteFeedListBinding binding;
    private FavoriteFeedListAdapter adapter;

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    private final FooterRecyclerViewAdapter.OnMaxPageScrollListener listener
            = new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
        @Override
        public void onMaxPageScrolled() {
            if (nowGettingNextEntry) return;
            nowGettingNextEntry = true;

            counter++;
            subscriptions.add(getEntries()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Entries>() {
                        @Override
                        public void onCompleted() {
                            nowGettingNextEntry = false;
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            nowGettingNextEntry = false;
                            // TODO: error handling
                        }

                        @Override
                        public void onNext(Entries entries) {
                            if (entries == null || entries.isEmpty()) {
                                adapter.setFooterVisibility(false);
                            } else {
                                adapter.setFooterVisibility(true);
                                adapter.add(entries);
                                // TODO: ちらつく
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }));
        }
    };

    @Override
    public void onDestroyView() {
        subscriptions.unsubscribe();
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_feed_list, container, false);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(AllMemberActivity.getChooserIntent(getContext()), REQUEST_MEMBER_CHOOSER);
            }
        });
        binding.recyclerview.setHasFixedSize(true); // アイテムは固定サイズ
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFavoriteMemberFeed();
            }
        });
        getFavoriteMemberFeed();

        return binding.getRoot();
    }

    private void getFavoriteMemberFeed() {
        counter = 0;
        setVisibilityEmptyView(false);

        subscriptions.add(getEntries()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        binding.refresh.setRefreshing(true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Entries>() {
                    @Override
                    public void onCompleted() {
                        binding.refresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        binding.refresh.setRefreshing(false);
                        // TODO: error handling
                    }

                    @Override
                    public void onNext(Entries entries) {
                        if (entries.isEmpty()) {
                            setVisibilityEmptyView(true);
                            return;
                        }
                        adapter = new FavoriteFeedListAdapter(getActivity(), entries);
                        adapter.setOnMaxPageScrollListener(listener);
                        binding.recyclerview.setAdapter(adapter);
                        setVisibilityEmptyView(false);
                    }
                }));
    }

    private Observable<Entries> getEntries() {
        return Favorites.getFavorites(getContext())
                .map(new Func1<Favorites, List<Integer>>() {
                    @Override
                    public List<Integer> call(Favorites favorites) {
                        return favorites.getMemberIdList();
                    }
                })
                .flatMap(new Func1<List<Integer>, Observable<Entries>>() {
                    @Override
                    public Observable<Entries> call(List<Integer> integers) {
                        return KeyakiFeedApiClient.getMemberEntries(
                                integers, (PAGE_LIMIT * counter), PAGE_LIMIT);
                    }
                })
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
        if (requestCode == REQUEST_MEMBER_CHOOSER && resultCode == Activity.RESULT_OK) {
            getFavoriteMemberFeed();
        }
    }

    private static class FavoriteFeedListAdapter extends FooterRecyclerViewAdapter<Entry, ListItemCardBinding> {

        private static final String TAG = FavoriteFeedListAdapter.class.getSimpleName();

        FavoriteFeedListAdapter(Context context, List<Entry> list) {
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
                            BlogActivity.getStartIntent(getContext(), entry));
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
