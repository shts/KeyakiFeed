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
import android.widget.LinearLayout;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentAllFeedListBinding;
import jp.shts.android.keyakifeed.databinding.ListItemEntryBinding;
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.providers.FavoriteContentObserver;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AllFeedListFragment extends Fragment {

    private static final String TAG = AllFeedListFragment.class.getSimpleName();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;

    private FragmentAllFeedListBinding binding;
    private LinearLayout footerView;
    private AllFeedListAdapter allFeedListAdapter;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    private final FavoriteContentObserver favoriteContentObserver = new FavoriteContentObserver() {
        @Override
        public void onChangeState(@State int state) {
            if (allFeedListAdapter != null) allFeedListAdapter.notifyDataSetChanged();
        }
    };

    private final AllFeedListAdapter.OnPageMaxScrolledListener pageMaxScrolledListener
            = new AllFeedListAdapter.OnPageMaxScrolledListener() {
        @Override
        public void onScrolledMaxPage() {
            getNextFeed();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        favoriteContentObserver.register(getContext());
    }

    @Override
    public void onDestroy() {
        favoriteContentObserver.unregister(getContext());
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_feed_list, container, false);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getEntries();
            }
        });
        binding.allFeedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = (Entry) parent.getItemAtPosition(position);
                getActivity().startActivity(
                        BlogActivity.getStartIntent(getContext(), entry));
            }
        });
        footerView = (LinearLayout) inflater.inflate(R.layout.list_item_more_load, null);
        footerView.setVisibility(View.GONE);
        binding.allFeedList.addFooterView(footerView);

        getEntries();
        return binding.getRoot();
    }

    private void getEntries() {
        counter = 0;
        subscriptions.add(KeyakiFeedApiClient.getAllEntries(counter, PAGE_LIMIT)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        binding.refresh.setRefreshing(true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Entries>() {
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
                    public void onNext(Entries entries) {
                        if (entries == null || entries.isEmpty()) {
                            return;
                        }
                        allFeedListAdapter = new AllFeedListAdapter(getActivity(), entries);
                        allFeedListAdapter.setPageMaxScrolledListener(pageMaxScrolledListener);
                        binding.allFeedList.setAdapter(allFeedListAdapter);
                    }
                }));
    }

    private void getNextFeed() {
        counter++;
        subscriptions.add(KeyakiFeedApiClient.getAllEntries((counter * PAGE_LIMIT), PAGE_LIMIT)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        footerView.setVisibility(View.VISIBLE);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Entries>() {
                    @Override
                    public void onCompleted() {
                        footerView.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        footerView.setVisibility(View.GONE);
                        // TODO: error handling
                    }

                    @Override
                    public void onNext(Entries entries) {
                        if (entries == null || entries.isEmpty()) {
                            return;
                        }
                        if (allFeedListAdapter != null) {
                            allFeedListAdapter.addAll(entries);
                            allFeedListAdapter.notifyDataSetChanged();
                        }
                    }
                }));
    }

    private static class AllFeedListAdapter extends ArrayAdapter<Entry> {

        private static final String TAG = AllFeedListAdapter.class.getSimpleName();

        private OnPageMaxScrolledListener pageMaxScrolledListener;
        private LayoutInflater inflater;

        AllFeedListAdapter(Context context, List<Entry> list) {
            super(context, -1, list);
            inflater = LayoutInflater.from(context);
        }

        interface OnPageMaxScrolledListener {
            void onScrolledMaxPage();
        }

        void setPageMaxScrolledListener(OnPageMaxScrolledListener listener) {
            pageMaxScrolledListener = listener;
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
                    if (entry != null) {
                        context.startActivity(
                                MemberDetailActivity.getStartIntent(context, entry.getMemberId()));
                    }
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
