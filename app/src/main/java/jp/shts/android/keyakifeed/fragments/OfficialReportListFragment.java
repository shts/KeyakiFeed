package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.adapters.ArrayRecyclerAdapter;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentOfficialReportBinding;
import jp.shts.android.keyakifeed.databinding.ListItemReportBinding;
import jp.shts.android.keyakifeed.models.Report;
import jp.shts.android.keyakifeed.models.Reports;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class OfficialReportListFragment extends Fragment {

    private static final String TAG = OfficialReportListFragment.class.getSimpleName();

    private FragmentOfficialReportBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public static OfficialReportListFragment newInstance() {
        return new OfficialReportListFragment();
    }

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_official_report, container, false);
        binding.recyclerview.setHasFixedSize(true);
        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                subscriptions.add(getAllReports()
                        .subscribe(new Action1<Reports>() {
                            @Override
                            public void call(Reports reports) {
                                binding.recyclerview.setAdapter(
                                        new OfficialReportListAdapter(getContext(), reports));
                            }
                        }));
            }
        });
        binding.refresh.setColorSchemeResources(R.color.primary);

        subscriptions.add(getAllReports()
                .subscribe(new Action1<Reports>() {
                    @Override
                    public void call(Reports reports) {
                        binding.recyclerview.setAdapter(
                                new OfficialReportListAdapter(getContext(), reports));
                    }
                }));

        return binding.getRoot();
    }

    @CheckResult
    private Observable<Reports> getAllReports() {
        return KeyakiFeedApiClient.getAllReports(0, 30)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnRequest(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        binding.refresh.setRefreshing(true);
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

    public static class OfficialReportListAdapter
            extends ArrayRecyclerAdapter<Report, BindingHolder<ListItemReportBinding>> {

        private static final String TAG = OfficialReportListAdapter.class.getSimpleName();

        public OfficialReportListAdapter(@NonNull Context context, @NonNull Reports reports) {
            super(context);
            addAll(reports);
        }

        @Override
        public BindingHolder<ListItemReportBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BindingHolder<>(getContext(), parent, R.layout.list_item_report);
        }

        @Override
        public void onBindViewHolder(BindingHolder<ListItemReportBinding> holder, int position) {
            final Report report = getItem(position);
            ListItemReportBinding binding = holder.binding;
            binding.setReport(report);

            final View root = binding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(
                            BlogActivity.getStartIntent(getContext(), report));
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (root.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                    ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
                    p.setMargins(8, 8, 8, 8);
                    root.requestLayout();
                }
            }
        }
    }
}
