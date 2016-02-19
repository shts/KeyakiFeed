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

public class MatomeBrowseFragment extends Fragment {

    private static final String TAG = MatomeBrowseFragment.class.getSimpleName();

    public static MatomeBrowseFragment newInstance(FeedItem feedItem) {
        MatomeBrowseFragment fragment = new MatomeBrowseFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("feedItem", feedItem);
        fragment.setArguments(bundle);
        return fragment;
    }

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

        final WebView webView = (WebView) view.findViewById(R.id.browser);
        webView.loadUrl(feedItem.url);

        return view;
    }
}
