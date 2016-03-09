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

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MatomeBrowseActivity;
import jp.shts.android.keyakifeed.adapters.MatomeFeedListAdapter;
import jp.shts.android.keyakifeed.api.MatomeFeedClient;
import jp.shts.android.keyakifeed.databinding.FragmentMatomeFeedListBinding;
import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class MatomeFeedListFragment extends Fragment {

    private static final String TAG = MatomeFeedListFragment.class.getSimpleName();

    private FragmentMatomeFeedListBinding binding;

    @Override
    public void onPause() {
        super.onPause();
        BusHolder.get().unregister(this);
        binding.adView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
        binding.adView.resume();
    }

    @Override
    public void onDestroy() {
        binding.adView.destroy();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_matome_feed_list, container, false);

        binding.refresh.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                MatomeFeedClient.get();
            }
        });
        binding.refresh.post(new Runnable() {
            @Override public void run() {
                binding.refresh.setRefreshing(true);
            }
        });

        MatomeFeedClient.get();

        return binding.getRoot();
    }

    @Subscribe
    public void onGotMatomeFeedList(MatomeFeedClient.GetMatomeFeedCallback callback) {
        if (binding.refresh != null) {
            if (binding.refresh.isRefreshing()) {
                binding.refresh.setRefreshing(false);
            }
        }
        if (callback.hasError()) {
            Log.d(TAG, "has error!!!");
            return;
        }
        binding.matomeFeedList.setAdapter(new MatomeFeedListAdapter(getActivity(), callback.feedItemList));
        binding.matomeFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem feedItem = (FeedItem) parent.getItemAtPosition(position);
                final Context context = getActivity();
                context.startActivity(MatomeBrowseActivity.getStartIntent(context, feedItem));
            }
        });
    }
}
