package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.databinding.FragmentAllFeedListBinding;
import jp.shts.android.keyakifeed.databinding.ListItemEntryBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class AllFeedListFragment extends Fragment {

    private static final String TAG = AllFeedListFragment.class.getSimpleName();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;

    private FragmentAllFeedListBinding binding;
    private LinearLayout footerView;
    private AllFeedListAdapter allFeedListAdapter;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_feed_list, container, false);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllEntries();
            }
        });
        binding.refresh.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        binding.refresh.post(new Runnable() {
            @Override public void run() {
                getAllEntries();
                binding.refresh.setRefreshing(true);
            }
        });
        binding.allFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = (Entry) parent.getItemAtPosition(position);
                getActivity().startActivity(BlogActivity.getStartIntent(getActivity(), new Blog(entry)));
            }
        });
        footerView = (LinearLayout) inflater.inflate(R.layout.list_item_more_load, null);
        footerView.setVisibility(View.GONE);

        binding.allFeedList.addFooterView(footerView);
        return binding.getRoot();
    }

    private void getAllEntries() {
        counter = 0;
        Entry.all(Entry.getQuery(PAGE_LIMIT, counter));
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.All all) {
        if (binding.refresh != null) {
            if (binding.refresh.isRefreshing()) {
                binding.refresh.setRefreshing(false);
            }
        }
        if (all.hasError()) {
            Log.e(TAG, "cannot get entries", all.e);
            return;
        }
        allFeedListAdapter = new AllFeedListAdapter(getActivity(), all.entries);
        allFeedListAdapter.setPageMaxScrolledListener(new AllFeedListAdapter.OnPageMaxScrolledListener() {
            @Override
            public void onScrolledMaxPage() {
                getNextFeed();
            }
        });
        binding.allFeedList.setAdapter(allFeedListAdapter);
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
        if (binding.refresh != null) {
            if (binding.refresh.isRefreshing()) {
                binding.refresh.setRefreshing(false);
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

    public static class AllFeedListAdapter extends ArrayAdapter<Entry> {

        private static final String TAG = AllFeedListAdapter.class.getSimpleName();

        private OnPageMaxScrolledListener pageMaxScrolledListener;
        private LayoutInflater inflater;

        public AllFeedListAdapter(Context context, List<Entry> list) {
            super(context, -1, list);
            inflater = LayoutInflater.from(context);
        }

        public interface OnPageMaxScrolledListener {
            void onScrolledMaxPage();
        }

        public void setPageMaxScrolledListener(OnPageMaxScrolledListener listener) {
            pageMaxScrolledListener = listener;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemEntryBinding binding;

            if (convertView == null) {
                binding = DataBindingUtil.inflate(inflater, R.layout.list_item_entry, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemEntryBinding) convertView.getTag();
            }

            final Entry entry = getItem(position);
            binding.setEntry(entry);
            binding.profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Context context = getContext();
                    context.startActivity(
                            MemberDetailActivity.getStartIntent(context, entry.getMemberId()));
                }
            });

            if (getCount() - 1 <= position) {
                if (pageMaxScrolledListener != null) {
                    pageMaxScrolledListener.onScrolledMaxPage();
                }
            }
            return convertView;
        }
    }
}
