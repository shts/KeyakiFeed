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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.GalleryActivity;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentMemberImageGridBinding;
import jp.shts.android.keyakifeed.databinding.ListItemImageGridBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.models2.Entries;
import jp.shts.android.keyakifeed.models2.Entry;
import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.services.DownloadImageService;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class MemberImageGridFragment extends Fragment {

    private static final String TAG = MemberImageGridFragment.class.getSimpleName();

    public static MemberImageGridFragment newInstance(Member member) {
        MemberImageGridFragment memberImageGridFragment = new MemberImageGridFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("member", member);
        memberImageGridFragment.setArguments(bundle);
        return memberImageGridFragment;
    }

    private static final int PAGE_LIMIT = 30;
    private int counter = 0;
    private boolean nowGettingNextEntry;

    private FragmentMemberImageGridBinding binding;
    private MemberImageGridAdapter adapter;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_member_image_grid, container, false);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DownloadConfirmDialog downloadConfirmDialog = new DownloadConfirmDialog();
                downloadConfirmDialog.setDialogTitle("画像ダウンロード");
                downloadConfirmDialog.setDialogMessage("このメンバーのすべての画像をダウンロードしますか");
                downloadConfirmDialog.setCallbacks(new DownloadConfirmDialog.Callbacks() {
                    @Override
                    public void onClickPositiveButton() {
                        Log.v(TAG, "onClickPositiveButton");
                        DownloadImageService.download(
                                getActivity().getApplicationContext()
                                , getArguments().getString("memberObjectId"));
                    }

                    @Override
                    public void onClickNegativeButton() {
                    }
                });
                downloadConfirmDialog.show(getFragmentManager(), TAG);
            }
        });

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
                .map(new Func1<Entries, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call(Entries entries) {
                        ArrayList<String> list = new ArrayList<>();
                        for (Entry e : entries) {
                            list.addAll(e.getImageUrlList());
                        }
                        return list;
                    }
                })
                .onErrorReturn(new Func1<Throwable, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call(Throwable throwable) {
                        return null;
                    }
                })
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> list) {
                        if (list == null || list.isEmpty()) {
                            Log.e(TAG, "cannot get entries");
                        } else {
                            // setup adapter
                            adapter = new MemberImageGridAdapter(getContext(), list);
                            adapter.setOnMaxPageScrollListener(new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
                                @Override
                                public void onMaxPageScrolled() {
                                    Log.v(TAG, "onMaxPageScrolled() : nowGettingNextEntry(" + nowGettingNextEntry + ")");
                                    if (nowGettingNextEntry) return;
                                    nowGettingNextEntry = true;
                                    // get next feed
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
                .map(new Func1<Entries, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call(Entries entries) {
                        ArrayList<String> list = new ArrayList<>();
                        for (Entry e : entries) {
                            list.addAll(e.getImageUrlList());
                        }
                        return list;
                    }
                })
                .onErrorReturn(new Func1<Throwable, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call(Throwable throwable) {
                        Snackbar.make(binding.coordinator, "ブログ記事の取得に失敗しました。通信状態を確認してください。", Snackbar.LENGTH_SHORT).show();
                        throwable.printStackTrace();
                        return new ArrayList<>();
                    }
                })
                .subscribe(new Action1<ArrayList<String>>() {
                    @Override
                    public void call(ArrayList<String> list) {
                        nowGettingNextEntry = false;
                        if (list == null || list.isEmpty()) {
                            Log.e(TAG, "cannot get entries");
                            if (adapter != null) adapter.setFoooterVisibility(false);
                        } else {
                            if (adapter != null) {
                                adapter.setFoooterVisibility(true);
                                adapter.add(list);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }));
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
                BindingHolder<ListItemImageGridBinding> bindingHolder, final String url) {
            final ListItemImageGridBinding binding = bindingHolder.binding;
            binding.setUrl(url);
            final View root = binding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(
                            GalleryActivity.getStartIntent(
                                    getContext(), (ArrayList<String>) getList(), getPosition(url)));
                }
            });
        }
    }
}
