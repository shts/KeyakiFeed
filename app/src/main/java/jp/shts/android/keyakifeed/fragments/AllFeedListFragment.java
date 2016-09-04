package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.CheckResult;
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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
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
            subscriptions.add(getNextFeed().subscribe(new Action1<Entries>() {
                @Override
                public void call(Entries entries) {
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
        binding.refresh.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
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
        subscriptions.add(getAllEntries().subscribe(new Action1<Entries>() {
            @Override
            public void call(Entries entries) {
                if (entries == null || entries.isEmpty()) {
                    return;
                }
                allFeedListAdapter = new AllFeedListAdapter(getActivity(), entries);
                allFeedListAdapter.setPageMaxScrolledListener(pageMaxScrolledListener);
                binding.allFeedList.setAdapter(allFeedListAdapter);
            }
        }));
    }

    @CheckResult
    private Observable<Entries> getAllEntries() {
        counter = 0;
        return KeyakiFeedApiClient.getAllEntries(counter, PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (binding.refresh.isRefreshing()) {
                            binding.refresh.setRefreshing(true);
                        }
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (binding.refresh.isRefreshing()) {
                            binding.refresh.setRefreshing(false);
                        }
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        if (binding.refresh.isRefreshing()) {
                            binding.refresh.setRefreshing(false);
                        }
                    }
                });
    }

    @CheckResult
    private Observable<Entries> getNextFeed() {
        counter++;
        return KeyakiFeedApiClient.getAllEntries((counter * PAGE_LIMIT), PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        footerView.setVisibility(View.VISIBLE);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        footerView.setVisibility(View.GONE);
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        footerView.setVisibility(View.GONE);
                    }
                });
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
