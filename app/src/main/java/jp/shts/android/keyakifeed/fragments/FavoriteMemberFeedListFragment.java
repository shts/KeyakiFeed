package jp.shts.android.keyakifeed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.AllMemberActivity;
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

    /**
     * Cache for page change
     */
    private static List<Entry> cache;

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHolder.get().unregister(this);
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
                // clear cache
                cache = null;
                setupFavoriteMemberFeed();
            }
        });
        multiSwipeRefreshLayout.setSwipeableChildren(R.id.recyclerview, R.id.empty_view);
        multiSwipeRefreshLayout.setColorSchemeResources(
                R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        setupFavoriteMemberFeed();

        if (cache != null) {
            recyclerView.setAdapter(new FavoriteFeedListAdapter(getActivity(), cache));
            setVisibilityEmptyView(false);
            if (multiSwipeRefreshLayout.isRefreshing()) {
                multiSwipeRefreshLayout.setRefreshing(false);
            }
        }
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
        List<String> ids = new ArrayList<>();
        for (Favorite favorite : callback.favorites) {
            ids.add(favorite.getMemberObjectId());
        }
        final ParseQuery<Entry> query = Entry.getQuery(30, 0);
        query.whereContainedIn("author_id", ids);
        Entry.all(query);
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.All all) {
        if (multiSwipeRefreshLayout.isRefreshing()) {
            multiSwipeRefreshLayout.setRefreshing(false);
        }
        if (all.e != null || all.entries == null || all.entries.isEmpty()) {
            setVisibilityEmptyView(true);
            return;
        }
        cache = all.entries;
        recyclerView.setAdapter(new FavoriteFeedListAdapter(getActivity(), all.entries));
        setVisibilityEmptyView(false);
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
