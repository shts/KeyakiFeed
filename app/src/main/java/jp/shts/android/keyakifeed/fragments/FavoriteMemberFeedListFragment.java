package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
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
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.models2.Entries;
import jp.shts.android.keyakifeed.models2.Entry;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * TODO:
 * メンバー一覧とこのページを行き来するだけでクソ重い
 * Skipped 32 frames!  The application may be doing too much work on its main thread
 */
public class FavoriteMemberFeedListFragment extends Fragment {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();

    private FragmentFavoriteFeedListBinding binding;

    private FavoriteFeedListAdapter adapter;

    private List<Integer> favoriteMemberIdList = new ArrayList<>();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusHolder.get().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (changeFavoriteState) setupFavoriteMemberFeed();
        changeFavoriteState = false;
    }

    private boolean changeFavoriteState;

    @Subscribe
    public void onChangedFavoriteState(Favorite.ChangedFavoriteState favoriteState) {
        // TODO: use data observe
        changeFavoriteState = true;
    }

    @Override
    public void onDestroy() {
        BusHolder.get().unregister(this);
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
                Intent intent = AllMemberActivity.getChooserIntent(getContext());
                getContext().startActivity(intent);
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
        binding.refresh.setColorSchemeResources(
                R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        binding.refresh.post(new Runnable() {
            @Override
            public void run() {
                binding.refresh.setRefreshing(true);
                setupFavoriteMemberFeed();
            }
        });

        return binding.getRoot();
    }

    private void setupFavoriteMemberFeed() {
        setVisibilityEmptyView(false);
        ParseQuery<Favorite> query = ParseQuery.getQuery(Favorite.class);
        query.fromLocalDatastore();
        Favorite.all(query);
    }

    @Subscribe
    public void GotAllFavorites(Favorite.GetFavoritesCallback callback) {
        if (callback.e != null || callback.favorites == null || callback.favorites.isEmpty()) {
            setVisibilityEmptyView(true);
            if (binding.refresh.isRefreshing()) {
                binding.refresh.setRefreshing(false);
            }
            return;
        }
        favoriteMemberIdList.clear();
        for (Favorite favorite : callback.favorites) {
            favoriteMemberIdList.add(favorite.getMemberObjectId());
        }

        counter = 0;
        Subscription subscription = getEntries().subscribe(new Action1<Entries>() {
            @Override
            public void call(Entries entries) {
                if (binding.refresh.isRefreshing()) {
                    binding.refresh.setRefreshing(false);
                }
                if (entries.isEmpty()) return;

                adapter = new FavoriteFeedListAdapter(getActivity(), entries);
                adapter.setOnMaxPageScrollListener(new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
                    @Override
                    public void onMaxPageScrolled() {
                        if (nowGettingNextEntry) return;
                        nowGettingNextEntry = true;
                        // get next feed
                        counter++;
                        Subscription subscription = getEntries().subscribe(new Action1<Entries>() {
                            @Override
                            public void call(Entries entries) {
                                if (entries == null || entries.isEmpty()) {
                                    Log.e(TAG, "cannot get entries");
                                    if (adapter != null) adapter.setFoooterVisibility(false);
                                } else {
                                    if (adapter != null) {
                                        adapter.setFoooterVisibility(true);
                                        adapter.add(entries);
                                        adapter.notifyDataSetChanged();
                                    }
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

    private Single<Entries> getEntries() {
        counter = 0;
        return KeyakiFeedApiClient.getMemberEntries(
                favoriteMemberIdList, (PAGE_LIMIT * counter), PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
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

    private void setVisibilityEmptyView(boolean isVisible) {
        if (isVisible) {
            // recyclerView を setVisiblity(View.GONE) で表示にするとプログレスが表示されない
            //mEntries.clear();
            binding.recyclerview.setAdapter(null);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.emptyView.setVisibility(View.GONE);
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
