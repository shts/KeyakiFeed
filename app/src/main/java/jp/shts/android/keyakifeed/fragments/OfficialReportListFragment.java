package jp.shts.android.keyakifeed.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.adapters.OfficialReportListAdapter;
import jp.shts.android.keyakifeed.databinding.FragmentOfficialReportBinding;
import jp.shts.android.keyakifeed.models.Report;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class OfficialReportListFragment extends Fragment {

    private static final String TAG = OfficialReportListFragment.class.getSimpleName();

    private FragmentOfficialReportBinding binding;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_official_report, container, false);

        binding.recyclerview.setHasFixedSize(true);

        binding.refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Report.all(Report.getQuery());
            }
        });
        binding.refresh.setColorSchemeResources(
                R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        binding.refresh.post(new Runnable() {
            @Override
            public void run() {
                Report.all(Report.getQuery());
                binding.refresh.setRefreshing(true);
            }
        });
        return binding.getRoot();
    }

    @Subscribe
    public void onGotAllReports(Report.GetReportsCallback callback) {
        if (binding.refresh.isRefreshing()) {
            binding.refresh.setRefreshing(false);
        }
        if (callback.hasError()) {
            return;
        }
        final OfficialReportListAdapter adapter = new OfficialReportListAdapter(getContext());
        adapter.addAll(callback.reports);
        binding.recyclerview.setAdapter(adapter);
    }
}
