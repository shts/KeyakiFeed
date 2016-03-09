package jp.shts.android.keyakifeed.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.FragmentLicensesBinding;

public class LisencesFragment extends Fragment {

    private static final String TAG = LisencesFragment.class.getSimpleName();

    private FragmentLicensesBinding binding;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_licenses, container, false);
        binding.toolbar.setTitle("オープンソースライセンス");
        binding.toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        binding.browser.loadUrl("file:///android_asset/licenses.html");
        return binding.getRoot();
    }
}
