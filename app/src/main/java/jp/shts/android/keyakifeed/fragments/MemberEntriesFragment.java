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

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentMemberEntriesBinding;
import jp.shts.android.keyakifeed.databinding.ListItemMemberDetailEntryBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.models2.Entries;
import jp.shts.android.keyakifeed.models2.Entry;
import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.views.DividerItemDecoration;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MemberEntriesFragment extends Fragment {

    private static final String TAG = MemberEntriesFragment.class.getSimpleName();

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;

    private MemberFeedListAdapter adapter;
    private FragmentMemberEntriesBinding binding;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public static MemberEntriesFragment newInstance(Member member) {
        MemberEntriesFragment memberEntriesFragment = new MemberEntriesFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("member", member);
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

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_member_entries, container, false);

        binding.recyclerView.setHasFixedSize(false);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getActivity()));

        getMemberEntries();

        return binding.getRoot();
    }

    private void getMemberEntries() {
        final Member member = getArguments().getParcelable("member");
        if (member == null) return;
        counter = 0;
        subscriptions.add(KeyakiFeedApiClient.getMemberEntries(
                member.getId(), counter, PAGE_LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Entries>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Entries entries) {
                        Log.v(TAG, "onNext");
                        if (entries == null || entries.isEmpty()) {
                            Log.e(TAG, "cannot get entries");
                        } else {
                            // setup adapter
                            adapter = new MemberFeedListAdapter(getContext(), entries);
                            adapter.setOnMaxPageScrollListener(new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
                                @Override
                                public void onMaxPageScrolled() {
                                    Log.v(TAG, "onMaxPageScrolled() : nowGettingNextEntry(" + nowGettingNextEntry + ")");
                                    if (nowGettingNextEntry) return;
                                    nowGettingNextEntry = true;
                                    getMemberNextEntries();
                                }
                            });
                            binding.recyclerView.setAdapter(adapter);
                        }
                    }
                }));
    }

    public void getMemberNextEntries() {
        final Member member = getArguments().getParcelable("member");
        if (member == null) return;
        counter++;
        subscriptions.add(KeyakiFeedApiClient.getMemberEntries(
                member.getId(), (PAGE_LIMIT * counter), PAGE_LIMIT)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Entries>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Entries entries) {
                        Log.v(TAG, "onNext");
                        nowGettingNextEntry = false;
                        if (entries == null || entries.isEmpty()) {
                            Log.e(TAG, "cannot get entries");
                        } else {
                            if (adapter != null) {
                                adapter.setFoooterVisibility(true);
                                adapter.add(entries);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }));
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
