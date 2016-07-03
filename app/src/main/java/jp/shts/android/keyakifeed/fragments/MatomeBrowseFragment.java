package jp.shts.android.keyakifeed.fragments;

import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.FragmentMatomeBrowseBinding;
import jp.shts.android.keyakifeed.models.Matome;
import jp.shts.android.keyakifeed.utils.ShareUtils;

public class MatomeBrowseFragment extends Fragment {

    private static final String TAG = MatomeBrowseFragment.class.getSimpleName();

    public static MatomeBrowseFragment newInstance(Matome matome) {
        MatomeBrowseFragment fragment = new MatomeBrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("matome", matome);
        fragment.setArguments(bundle);
        return fragment;
    }

    private FragmentMatomeBrowseBinding binding;

    @SuppressLint("SetJavaScriptEnabled")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_matome_browse, container, false);

        final Matome matome = getArguments().getParcelable("matome");
        if (matome == null || TextUtils.isEmpty(matome.getEntryUrl())) {
            Toast.makeText(getActivity().getApplicationContext(),
                    "記事の取得に失敗しました", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return null;
        }

        binding.toolbar.setTitle(matome.getEntryTitle());
        binding.toolbar.setSubtitle(matome.getFeedTitle());
        binding.toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(ShareUtils
                        .getShareMatomeBlogIntent(matome));
            }
        });

        binding.browser.getSettings().setJavaScriptEnabled(true);
        binding.browser.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });
        binding.browser.loadUrl(matome.getEntryUrl());

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.adView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.adView.pause();
    }

    @Override
    public void onDestroy() {
        binding.adView.destroy();
        super.onDestroy();
    }
}
