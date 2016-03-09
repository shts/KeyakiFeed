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
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter.OnMaxPageScrollListener;
import jp.shts.android.keyakifeed.databinding.FragmentFavoriteFeedListBinding;
import jp.shts.android.keyakifeed.databinding.ListItemCardBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class FavoriteMemberFeedListFragment extends Fragment {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();

    private FragmentFavoriteFeedListBinding binding;

    private FavoriteFeedListAdapter adapter;

    private List<String> favoriteMemberIdList = new ArrayList<>();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;

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
                setupFavoriteMemberFeed();
                binding.refresh.setRefreshing(true);
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
        final ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, counter);
        query.whereContainedIn("member_id", favoriteMemberIdList);
        Entry.all(query);
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.All all) {
        if (binding.refresh.isRefreshing()) {
            binding.refresh.setRefreshing(false);
        }
        if (all.hasError()) {
            setVisibilityEmptyView(true);
            return;
        }
        adapter = new FavoriteFeedListAdapter(getActivity(), all.entries);
        adapter.setOnMaxPageScrollListener(new OnMaxPageScrollListener() {
            @Override
            public void onMaxPageScrolled() {
                if (nowGettingNextEntry) return;
                nowGettingNextEntry = true;
                // get next feed
                counter++;
                ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, (PAGE_LIMIT * counter));
                query.whereContainedIn("member_id", favoriteMemberIdList);
                Entry.next(query);
            }
        });
        binding.recyclerview.setAdapter(adapter);
        setVisibilityEmptyView(false);
    }

    @Subscribe
    public void onGotNextEntries(Entry.GetEntriesCallback.Next next) {
        nowGettingNextEntry = false;
        if (next.hasError()) {
            Log.e(TAG, "cannot get entries", next.e);
            if (adapter != null) adapter.setFoooterVisibility(false);
            return;
        }
        if (adapter != null) {
            adapter.setFoooterVisibility(true);
            adapter.add(next.entries);
            adapter.notifyDataSetChanged();
        }
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
