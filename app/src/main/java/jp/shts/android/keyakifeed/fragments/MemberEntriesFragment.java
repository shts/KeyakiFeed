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
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.views.DividerItemDecoration;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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
                .onErrorReturn(new Func1<Throwable, Entries>() {
                    @Override
                    public Entries call(Throwable throwable) {
                        return new Entries();
                    }
                })
                .subscribe(new Action1<Entries>() {
                    @Override
                    public void call(Entries entries) {
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
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Func1<Throwable, Entries>() {
                    @Override
                    public Entries call(Throwable throwable) {
                        return new Entries();
                    }
                })
                .subscribe(new Action1<Entries>() {
                    @Override
                    public void call(Entries entries) {
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
