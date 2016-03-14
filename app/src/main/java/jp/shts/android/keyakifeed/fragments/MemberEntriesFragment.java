package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import jp.shts.android.keyakifeed.databinding.FragmentMemberEntriesBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMemberDetailEntryBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.views.DividerItemDecoration;

public class MemberEntriesFragment extends Fragment {

    private static final String TAG = MemberEntriesFragment.class.getSimpleName();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;

    private MemberFeedListAdapter adapter;
    private FragmentMemberEntriesBinding binding;

    public static MemberEntriesFragment newInstance(String memberObjectId) {
        MemberEntriesFragment memberEntriesFragment = new MemberEntriesFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberObjectId", memberObjectId);
        memberEntriesFragment.setArguments(bundle);
        return memberEntriesFragment;
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_member_entries, container, false);

        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, counter);
        Entry.findByIdAll(query, getArguments().getString("memberObjectId"));

        return binding.getRoot();
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.FindById.All callback) {
        if (callback.e != null) {
            //Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
            return;
        } else if (callback.entries == null || callback.entries.isEmpty()) {
            Log.v(TAG, "end of all entries !!!");
            return;
        }
        // setup adapter
        adapter = new MemberFeedListAdapter(getContext(), callback.entries);
        adapter.setOnMaxPageScrollListener(new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
            @Override
            public void onMaxPageScrolled() {
                Log.v(TAG, "onMaxPageScrolled() : nowGettingNextEntry(" + nowGettingNextEntry + ")");
                if (nowGettingNextEntry) return;
                nowGettingNextEntry = true;
                // get next feed
                counter++;
                ParseQuery<Entry> query = Entry.getQuery(PAGE_LIMIT, (PAGE_LIMIT * counter));
                Entry.findByIdNext(query, getArguments().getString("memberObjectId"));
            }
        });
        binding.recyclerView.setAdapter(adapter);
    }

    @Subscribe
    public void onGotNextEntries(Entry.GetEntriesCallback.FindById.Next callback) {
        nowGettingNextEntry = false;
        if (callback.e != null) {
            //Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
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

    private static class MemberFeedListAdapter extends FooterRecyclerViewAdapter<Entry, ListItemMemberDetailEntryBinding> {

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
            final ListItemMemberDetailEntryBinding detailEntryBinding = bindingHolder.binding;
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
