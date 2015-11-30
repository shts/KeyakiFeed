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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.AllMemberActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.FavoriteFeedListAdapter;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.views.MultiSwipeRefreshLayout;

public class FavoriteMemberFeedListFragment extends Fragment {

    private static final String TAG = FavoriteMemberFeedListFragment.class.getSimpleName();

    private MultiSwipeRefreshLayout multiSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private View emptyView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_feed_list, null);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
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
        setupFavoriteMemberFeed();
        return view;
    }

    private void setupFavoriteMemberFeed() {
        setVisibilityEmptyView(false);
        ParseQuery<Favorite> query = ParseQuery.getQuery(Favorite.class);
        query.fromLocalDatastore();
        query.findInBackground(new FindCallback<Favorite>() {
            @Override
            public void done(List<Favorite> favorites, ParseException e) {
                if (e != null || favorites == null || favorites.isEmpty()) {
                    if (multiSwipeRefreshLayout.isRefreshing()) {
                        multiSwipeRefreshLayout.setRefreshing(false);
                    }
                    setVisibilityEmptyView(true);
                    return;
                }
                List<String> ids = new ArrayList<>();
                for (Favorite favorite : favorites) {
                    ids.add(favorite.getMemberObjectId());
                }
                ParseQuery<Entry> query = Entry.getQuery(30, 0);
                query.whereContainedIn("author_id", ids);
                query.findInBackground(new FindCallback<Entry>() {
                    @Override
                    public void done(List<Entry> entries, ParseException e) {
                        if (e != null || entries == null || entries.isEmpty()) {
                            if (multiSwipeRefreshLayout.isRefreshing()) {
                                multiSwipeRefreshLayout.setRefreshing(false);
                            }
                            setVisibilityEmptyView(true);
                            return;
                        }
                        recyclerView.setAdapter(new FavoriteFeedListAdapter(getActivity(), entries));
                        setVisibilityEmptyView(false);
                        if (multiSwipeRefreshLayout.isRefreshing()) {
                            multiSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
            }
        });
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
