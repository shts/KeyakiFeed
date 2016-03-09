package jp.shts.android.keyakifeed.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.FragmentMatomeBrowseBinding;
import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.utils.ShareUtils;

public class MatomeBrowseFragment extends Fragment {

    private static final String TAG = MatomeBrowseFragment.class.getSimpleName();

    public static MatomeBrowseFragment newInstance(FeedItem feedItem) {
        MatomeBrowseFragment fragment = new MatomeBrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("feedItem", feedItem);
        fragment.setArguments(bundle);
        return fragment;
    }

    private FragmentMatomeBrowseBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_matome_browse, container, false);

        final FeedItem feedItem = getArguments().getParcelable("feedItem");

        binding.toolbar.setTitle(feedItem.title);
        binding.toolbar.setSubtitle(feedItem.siteTitle);
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
                getActivity().startActivity(ShareUtils.getShareMatomeBlogIntent(feedItem));
            }
        });

        binding.browser.loadUrl(feedItem.url);

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
