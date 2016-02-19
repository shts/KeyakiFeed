package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MatomeBrowseActivity;
import jp.shts.android.keyakifeed.adapters.MatomeFeedListAdapter;
import jp.shts.android.keyakifeed.api.MatomeFeedClient;
import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class MatomeFeedListFragment extends Fragment {

    private static final String TAG = MatomeFeedListFragment.class.getSimpleName();

    private ListView listView;

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
        View view = inflater.inflate(R.layout.fragment_matome_feed_list, null);
        listView = (ListView) view.findViewById(R.id.matome_feed_list);
        MatomeFeedClient.get();
        return view;
    }

    @Subscribe
    public void onGotMatomeFeedList(MatomeFeedClient.GetMatomeFeedCallback callback) {
        Log.i(TAG, "onGotMatomeFeedList");
        if (callback.hasError()) {
            Log.d(TAG, "has error!!!");
            return;
        }
        listView.setAdapter(new MatomeFeedListAdapter(getActivity(), callback.feedItemList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedItem feedItem = (FeedItem) parent.getItemAtPosition(position);
                final Context context = getActivity();
                context.startActivity(MatomeBrowseActivity.getStartIntent(context, feedItem));
            }
        });
    }
}
