package jp.shts.android.keyakifeed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter.OnMaxPageScrollListener;
import jp.shts.android.keyakifeed.adapters.FavoriteFeedListAdapter;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.views.MultiSwipeRefreshLayout;

public class FavoriteMemberFeedListFragment extends Fragment {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();

    private MultiSwipeRefreshLayout multiSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private View emptyView;
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
        View view = inflater.inflate(R.layout.fragment_favorite_feed_list, null);
        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = AllMemberActivity.getChooserIntent(getContext());
                getContext().startActivity(intent);
            }
        });
        emptyView = view.findViewById(R.id.empty_view);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true); // アイテムは固定サイズ
        // SwipeRefreshLayoutの設定
        multiSwipeRefreshLayout = (MultiSwipeRefreshLayout) view.findViewById(R.id.refresh);
        multiSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setupFavoriteMemberFeed();
            }
        });
        multiSwipeRefreshLayout.setSwipeableChildren(R.id.recyclerview, R.id.empty_view);
        multiSwipeRefreshLayout.setColorSchemeResources(
                R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        multiSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                setupFavoriteMemberFeed();
                multiSwipeRefreshLayout.setRefreshing(true);
            }
        });

        return view;
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
            if (multiSwipeRefreshLayout.isRefreshing()) {
                multiSwipeRefreshLayout.setRefreshing(false);
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
        if (multiSwipeRefreshLayout.isRefreshing()) {
            multiSwipeRefreshLayout.setRefreshing(false);
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
        recyclerView.setAdapter(adapter);
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
            recyclerView.setAdapter(null);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

}
