package jp.shts.android.keyakifeed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.entities.FeedItem;
import jp.shts.android.keyakifeed.utils.ShareUtils;
import jp.shts.android.keyakifeed.views.KeyakiFeedAdView;

public class MatomeBrowseFragment extends Fragment {

    private static final String TAG = MatomeBrowseFragment.class.getSimpleName();

    public static MatomeBrowseFragment newInstance(FeedItem feedItem) {
        MatomeBrowseFragment fragment = new MatomeBrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("feedItem", feedItem);
        fragment.setArguments(bundle);
        return fragment;
    }

    private KeyakiFeedAdView keyakiFeedAdView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_matome_browse, null);

        final FeedItem feedItem = getArguments().getParcelable("feedItem");

        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(feedItem.title);
        toolbar.setSubtitle(feedItem.siteTitle);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(ShareUtils.getShareMatomeBlogIntent(feedItem));
            }
        });

        keyakiFeedAdView = (KeyakiFeedAdView) view.findViewById(R.id.ad_view);

        final WebView webView = (WebView) view.findViewById(R.id.browser);
        webView.loadUrl(feedItem.url);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        keyakiFeedAdView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        keyakiFeedAdView.pause();
    }

    @Override
    public void onDestroy() {
        keyakiFeedAdView.destroy();
        super.onDestroy();
    }
}
