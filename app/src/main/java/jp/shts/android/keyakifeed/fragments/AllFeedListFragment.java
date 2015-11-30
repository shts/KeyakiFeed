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

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.adapters.AllFeedListAdapter;
import jp.shts.android.keyakifeed.models.Entry;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_feed_list, null);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllFeeds();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        listView = (ListView) view.findViewById(R.id.all_feed_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = (Entry) parent.getItemAtPosition(position);
                getActivity().startActivity(BlogActivity.getStartIntent(getActivity(), entry.getObjectId()));
            }
        });
        footerView = (LinearLayout) inflater.inflate(R.layout.list_item_more_load, null);
        footerView.setVisibility(View.GONE);

        listView.addFooterView(footerView);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAllFeeds();
    }

    private void getAllFeeds() {
        Log.v(TAG, "getAllFeeds start");
        Entry.getQuery(PAGE_LIMIT, counter).findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                if (e != null || entries == null || entries.isEmpty()) {
                    Log.e(TAG, "cannot get entries", e);
                    return;
                }
                allFeedListAdapter = new AllFeedListAdapter(getActivity(), entries);
                allFeedListAdapter.setPageMaxScrolledListener(scrolledListener);
                listView.setAdapter(allFeedListAdapter);
                if (swipeRefreshLayout != null) {
                    if (swipeRefreshLayout.isRefreshing()) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        });
    }

    private void getNextFeed() {
        if (footerView != null) {
            footerView.setVisibility(View.VISIBLE);
        }
        counter++;
        Entry.getQuery(PAGE_LIMIT, (counter * PAGE_LIMIT)).findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                if (e != null || entries == null || entries.isEmpty()) {
                    Log.e(TAG, "cannot get entries", e);
                    return;
                }
                if (allFeedListAdapter != null) {
                    allFeedListAdapter.addAll(entries);
                    allFeedListAdapter.notifyDataSetChanged();
                }
                if (footerView != null) {
                    footerView.setVisibility(View.GONE);
                }
            }
        });
    }
}
