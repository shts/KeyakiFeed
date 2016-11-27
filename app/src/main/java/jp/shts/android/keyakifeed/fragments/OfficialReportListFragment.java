package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
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
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

// TODO: ページネーションを実装する
public class OfficialReportListFragment extends Fragment {

    private static final String TAG = OfficialReportListFragment.class.getSimpleName();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;

    private FragmentOfficialReportBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

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
                getAllReports();
            }
        });

        getAllReports();
        return binding.getRoot();
    }

    private void getAllReports() {
        counter = 0;
        subscriptions.add(KeyakiFeedApiClient.getAllReports((counter * PAGE_LIMIT), PAGE_LIMIT)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        binding.refresh.setRefreshing(true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Reports>() {
                    @Override
                    public void onCompleted() {
                        binding.refresh.setRefreshing(false);

                    }

                    @Override
                    public void onError(Throwable e) {
                        binding.refresh.setRefreshing(false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Reports reports) {
                        binding.recyclerview.setAdapter(
                                new OfficialReportListAdapter(getContext(), reports));
                    }
                }));
    }

    private static class OfficialReportListAdapter
            extends ArrayRecyclerAdapter<Report, BindingHolder<ListItemReportBinding>> {

        private static final String TAG = OfficialReportListAdapter.class.getSimpleName();

        OfficialReportListAdapter(@NonNull Context context, @NonNull Reports reports) {
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
