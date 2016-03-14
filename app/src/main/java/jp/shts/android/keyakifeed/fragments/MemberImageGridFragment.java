package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseQuery;
import com.squareup.otto.Subscribe;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter;
import jp.shts.android.keyakifeed.databinding.FragmentMemberImageGridBinding;
import jp.shts.android.keyakifeed.databinding.ListItemImageGridBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class MemberImageGridFragment extends Fragment {

    private static final String TAG = MemberImageGridFragment.class.getSimpleName();

    public static MemberImageGridFragment newInstance(String memberObjectId) {
        MemberImageGridFragment memberImageGridFragment = new MemberImageGridFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberObjectId", memberObjectId);
        memberImageGridFragment.setArguments(bundle);
        return memberImageGridFragment;
    }

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;

    private FragmentMemberImageGridBinding binding;
    private MemberImageGridAdapter adapter;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_member_image_grid, container, false);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DownloadConfirmDialog downloadConfirmDialog = new DownloadConfirmDialog();
                downloadConfirmDialog.setCallbacks(new DownloadConfirmDialog.Callbacks() {
                    @Override
                    public void onClickPositiveButton() {

                    }
                    @Override
                    public void onClickNegativeButton() {

                    }
                });
                downloadConfirmDialog.show(getFragmentManager(), TAG);
            }
        });
        return binding.getRoot();
    }

    @Subscribe
    public void onGotAllEntries(Entry.GetEntriesCallback.FindById.All callback) {
        if (callback.e != null) {
            Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
            return;
        } else if (callback.entries == null || callback.entries.isEmpty()) {
            Log.v(TAG, "end of all entries !!!");
            return;
        }
        // setup adapter
        adapter = new MemberImageGridAdapter(getContext(), callback.getAllThumbnailUrlList());
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
            Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
            return;
        } else if (callback.entries == null || callback.entries.isEmpty()) {
            Log.v(TAG, "end of all entries !!!");
            if (adapter != null) adapter.setFoooterVisibility(false);
            return;
        }
        if (adapter != null) {
            adapter.setFoooterVisibility(true);
            adapter.add(callback.getAllThumbnailUrlList());
            adapter.notifyDataSetChanged();
        }
    }

    private static class MemberImageGridAdapter
            extends FooterRecyclerViewAdapter<String, ListItemImageGridBinding> {

        public MemberImageGridAdapter(Context context, List<String> list) {
            super(context, list);
        }

        @Override
        public BindingHolder<ListItemImageGridBinding>
                onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new BindingHolder<>(getContext(), parent, R.layout.list_item_image_grid);
        }

        @Override
        public void onBindContentItemViewHolder(
                BindingHolder<ListItemImageGridBinding> bindingHolder, String url) {
            final ListItemImageGridBinding binding = bindingHolder.binding;
            binding.setUrl(url);

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
