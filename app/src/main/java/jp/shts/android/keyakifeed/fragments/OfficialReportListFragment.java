package jp.shts.android.keyakifeed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.adapters.OfficialReportListAdapter;
import jp.shts.android.keyakifeed.models.Report;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class OfficialReportListFragment extends Fragment {

    private static final String TAG = OfficialReportListFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    public static OfficialReportListFragment newInstance() {
        return new OfficialReportListFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHolder.get().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_official_report, null);

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Report.all(Report.getQuery());
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                Report.all(Report.getQuery());
                swipeRefreshLayout.setRefreshing(true);
            }
        });
        return view;
    }

    @Subscribe
    public void onGotAllReports(Report.GetReportsCallback callback) {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
        if (callback.hasError()) {
            return;
        }
        final OfficialReportListAdapter adapter = new OfficialReportListAdapter(getContext());
        adapter.addAll(callback.reports);
        recyclerView.setAdapter(adapter);
    }
}
