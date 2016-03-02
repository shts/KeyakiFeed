package jp.shts.android.keyakifeed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.adapters.AllFeedListAdapter;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class AllFeedListFragment extends Fragment {

    private static final String TAG = AllFeedListFragment.class.getSimpleName();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private AllFeedListAdapter allFeedListAdapter;
    private LinearLayout footerView;

    private final AllFeedListAdapter.OnPageMaxScrolledListener scrolledListener
            = new AllFeedListAdapter.OnPageMaxScrolledListener() {
        @Override
        public void onScrolledMaxPage() {
            getNextFeed();
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        BusHolder.get().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_feed_list, null);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllEntries();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        swipeRefreshLayout.post(new Runnable() {
            @Override public void run() {
                getAllEntries();
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        listView = (ListView) view.findViewById(R.id.all_feed_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = (Entry) parent.getItemAtPosition(position);
                getActivity().startActivity(BlogActivity.getStartIntent(getActivity(), new Blog(entry)));
            }
        });
        footerView = (LinearLayout) inflater.inflate(R.layout.list_item_more_load, null);
        footerView.setVisibility(View.GONE);
        listView.addFooterView(footerView);
        return view;
    }

    private void getAllEntries() {
        counter = 0;
        Entry.all(Entry.getQuery(PAGE_LIMIT, counter));
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.All all) {
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        if (all.hasError()) {
            Log.e(TAG, "cannot get entries", all.e);
            return;
        }
        allFeedListAdapter = new AllFeedListAdapter(getActivity(), all.entries);
        allFeedListAdapter.setPageMaxScrolledListener(scrolledListener);
        listView.setAdapter(allFeedListAdapter);
    }

    private void getNextFeed() {
        if (footerView != null) {
            footerView.setVisibility(View.VISIBLE);
        }
        counter++;
        Entry.next(Entry.getQuery(PAGE_LIMIT, (counter * PAGE_LIMIT)));
    }

    @Subscribe
    public void onGotNextEntries(Entry.GetEntriesCallback.Next next) {
        if (footerView != null) {
            footerView.setVisibility(View.GONE);
        }
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        if (next.hasError()) {
            Log.e(TAG, "cannot get entries", next.e);
            return;
        }
        if (allFeedListAdapter != null) {
            allFeedListAdapter.addAll(next.entries);
            allFeedListAdapter.notifyDataSetChanged();
        }
    }

}
