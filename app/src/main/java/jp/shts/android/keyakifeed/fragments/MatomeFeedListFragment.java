package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MatomeBrowseActivity;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentMatomeFeedListBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMatomeFeedBinding;
import jp.shts.android.keyakifeed.models.Matome;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MatomeFeedListFragment extends Fragment {

    private static final String TAG = MatomeFeedListFragment.class.getSimpleName();

    private FragmentMatomeFeedListBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onPause() {
        super.onPause();
        binding.adView.pause();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.adView.resume();
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        binding.adView.destroy();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_matome_feed_list, container, false);

        binding.refresh.setColorSchemeResources(R.color.primary);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                subscriptions.add(getMatomeFeed());
            }
        });
        binding.refresh.post(new Runnable() {
            @Override public void run() {
                binding.refresh.setRefreshing(true);
            }
        });

        subscriptions.add(getMatomeFeed());

        return binding.getRoot();
    }

    private Subscription getMatomeFeed() {
        return KeyakiFeedApiClient.getMatomeFeeds()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Matome>>() {
                    @Override
                    public void onCompleted() {
                        if (binding.refresh != null) {
                            if (binding.refresh.isRefreshing()) {
                                binding.refresh.setRefreshing(false);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (binding.refresh != null) {
                            if (binding.refresh.isRefreshing()) {
                                binding.refresh.setRefreshing(false);
                            }
                        }
                    }

                    @Override
                    public void onNext(List<Matome> matomeList) {
                        binding.matomeFeedList.setAdapter(new MatomeFeedListAdapter(getActivity(), matomeList));
                        binding.matomeFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Matome matome = (Matome) parent.getItemAtPosition(position);
                                final Context context = getActivity();
                                context.startActivity(MatomeBrowseActivity.getStartIntent(context, matome));
                            }
                        });
                    }
                });
    }

    public static class MatomeFeedListAdapter extends ArrayAdapter<Matome> {

        private static final String TAG = MatomeFeedListAdapter.class.getSimpleName();

        private LayoutInflater inflater;

        public MatomeFeedListAdapter(Context context, List<Matome> list) {
            super(context, -1, list);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemMatomeFeedBinding binding;

            if (convertView == null) {
                binding = DataBindingUtil.inflate(
                        inflater, R.layout.list_item_matome_feed, parent, false);
                convertView = binding.getRoot();
                convertView.setTag(binding);
            } else {
                binding = (ListItemMatomeFeedBinding) convertView.getTag();
            }
            binding.setMatome(getItem(position));
            return convertView;
        }
    }
}
