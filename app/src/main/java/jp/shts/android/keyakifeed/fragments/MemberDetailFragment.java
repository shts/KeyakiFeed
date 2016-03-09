package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter.OnMaxPageScrollListener;
import jp.shts.android.keyakifeed.databinding.FragmentDetailMemberBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMemberDetailEntryBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.views.DividerItemDecoration;

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

    private FragmentDetailMemberBinding binding;
    private String memberObjectId;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_member, container, false);

        memberObjectId = getArguments().getString("memberObjectId");

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favorite.toggle(memberObjectId);
            }
        });

        binding.collapsingToolbar.setCollapsedTitleTextColor(
                ContextCompat.getColor(getContext(), android.R.color.white));
        binding.collapsingToolbar.setExpandedTitleColor(
                ContextCompat.getColor(getContext(), android.R.color.transparent));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        Member.fetch(memberObjectId);

        return binding.getRoot();
    }

    @Subscribe
    public void onFetchedMember(Member.FetchMemberCallback callback) {
        if (callback.e != null) {
            Log.e(TAG, "failed to get member : id(" + memberObjectId + ")", callback.e);
            return;
        }
        binding.viewMemberDetailHeader.setup(callback.member);
        binding.collapsingToolbar.setTitle(callback.member.getNameMain());

        // setup Entry list
        counter = 0;
        ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, counter);
        query.whereEqualTo("member_id", memberObjectId);
        Entry.all(query);
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.All callback) {
        if (callback.e != null) {
            Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
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
        binding.recyclerView.setAdapter(adapter);
    }

    @Subscribe
    public void onGotNextEntries(Entry.GetEntriesCallback.Next callback) {
        nowGettingNextEntry = false;
        if (callback.e != null) {
            Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(binding.coordinator, "推しメン登録しました", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinator, "推しメン登録を解除しました", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    public static class MemberFeedListAdapter extends FooterRecyclerViewAdapter<Entry, ListItemMemberDetailEntryBinding> {

        private static final String TAG = MemberFeedListAdapter.class.getSimpleName();

        public MemberFeedListAdapter(Context context, List<Entry> list) {
            super(context, list);
        }

        @Override
        public BindingHolder<ListItemMemberDetailEntryBinding> onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new BindingHolder<>(getContext(), parent, R.layout.list_item_member_detail_entry);
        }

        @Override
        public void onBindContentItemViewHolder(BindingHolder<ListItemMemberDetailEntryBinding> bindingHolder, final Entry entry) {
            ListItemMemberDetailEntryBinding detailEntryBinding = bindingHolder.binding;
            detailEntryBinding.setEntry(entry);

            final View root = detailEntryBinding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(
                            BlogActivity.getStartIntent(getContext(), new Blog(entry)));
                }
            });
        }

    }

}
