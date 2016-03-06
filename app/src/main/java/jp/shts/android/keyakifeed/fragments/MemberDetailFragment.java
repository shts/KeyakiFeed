package jp.shts.android.keyakifeed.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter.OnMaxPageScrollListener;
import jp.shts.android.keyakifeed.adapters.MemberFeedListAdapter;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.views.DividerItemDecoration;
import jp.shts.android.keyakifeed.views.MemberDetailHeader;

public class MemberDetailFragment extends Fragment {

    private static final String TAG = MemberDetailFragment.class.getSimpleName();

    public static MemberDetailFragment newInstance(String memberObjectId) {
        MemberDetailFragment memberDetailFragment = new MemberDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberObjectId", memberObjectId);
        memberDetailFragment.setArguments(bundle);
        return memberDetailFragment;
    }

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;

    private String memberObjectId;
    private RecyclerView recyclerView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private CoordinatorLayout coordinatorLayout;
    private MemberDetailHeader viewMemberDetailHeader;
    private MemberFeedListAdapter adapter;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_member, null);

        memberObjectId = getArguments().getString("memberObjectId");
        viewMemberDetailHeader = (MemberDetailHeader) view.findViewById(R.id.view_member_detail_header);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favorite.toggle(memberObjectId);
            }
        });

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator);

        collapsingToolbarLayout = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setCollapsedTitleTextColor(
                ContextCompat.getColor(getContext(), android.R.color.white));
        collapsingToolbarLayout.setExpandedTitleColor(
                ContextCompat.getColor(getContext(), android.R.color.transparent));

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        Member.fetch(memberObjectId);
        return view;
    }

    @Subscribe
    public void onFetchedMember(Member.FetchMemberCallback callback) {
        Log.v(TAG, "onFetchedMember");
        if (callback.e != null) {
            Log.e(TAG, "failed to get member : id(" + memberObjectId + ")", callback.e);
            return;
        }
        viewMemberDetailHeader.setup(callback.member);
        collapsingToolbarLayout.setTitle(callback.member.getNameMain());

        // setup Entry list
        counter = 0;
        ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, counter);
        query.whereEqualTo("member_id", memberObjectId);
        Entry.all(query);
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.All callback) {
        if (callback.e != null) {
            Snackbar.make(coordinatorLayout, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
            return;
        } else if (callback.entries == null || callback.entries.isEmpty()) {
            Log.v(TAG, "end of all entries !!!");
            return;
        }
        // setup adapter
        adapter = new MemberFeedListAdapter(getContext(), callback.entries);
        adapter.setOnMaxPageScrollListener(new OnMaxPageScrollListener() {
            @Override
            public void onMaxPageScrolled() {
                Log.v(TAG, "onMaxPageScrolled() : nowGettingNextEntry(" + nowGettingNextEntry + ")");
                if (nowGettingNextEntry) return;
                nowGettingNextEntry = true;
                // get next feed
                counter++;
                ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, (PAGE_LIMIT * counter));
                query.whereEqualTo("member_id", memberObjectId);
                Entry.next(query);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Subscribe
    public void onGotNextEntries(Entry.GetEntriesCallback.Next callback) {
        nowGettingNextEntry = false;
        if (callback.e != null) {
            Snackbar.make(coordinatorLayout, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
            return;
        } else if (callback.entries == null || callback.entries.isEmpty()) {
            Log.v(TAG, "end of all entries !!!");
            if (adapter != null) adapter.setFoooterVisibility(false);
            return;
        }
        if (adapter != null) {
            adapter.setFoooterVisibility(true);
            adapter.add(callback.entries);
            adapter.notifyDataSetChanged();
        }
    }

    @Subscribe
    public void onChangedFavoriteState(Favorite.ChangedFavoriteState state) {
        if (state.e == null) {
            if (state.action == Favorite.ChangedFavoriteState.Action.ADD) {
                Snackbar.make(coordinatorLayout, "推しメン登録しました", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, "推しメン登録を解除しました", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

}
