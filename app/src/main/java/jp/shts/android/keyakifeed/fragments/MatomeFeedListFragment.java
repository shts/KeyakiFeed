package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
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
    public void onDestroyView() {
        subscriptions.unsubscribe();
        binding.adView.destroy();
        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_matome_feed_list, container, false);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMatomeFeed();
            }
        });
        getMatomeFeed();
        return binding.getRoot();
    }

    private void getMatomeFeed() {
        subscriptions.add(KeyakiFeedApiClient.getMatomeFeeds()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        binding.refresh.setRefreshing(true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Matome>>() {
                    @Override
                    public void onCompleted() {
                        binding.refresh.setRefreshing(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        binding.refresh.setRefreshing(false);
                        // TODO: error handling
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
                }));
    }

    private static class MatomeFeedListAdapter extends ArrayAdapter<Matome> {

        private static final String TAG = MatomeFeedListAdapter.class.getSimpleName();

        private LayoutInflater inflater;

        MatomeFeedListAdapter(Context context, List<Matome> list) {
            super(context, -1, list);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
