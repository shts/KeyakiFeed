package jp.shts.android.keyakifeed.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.GalleryActivity;
import jp.shts.android.keyakifeed.activities.PermissionRequireActivity;
import jp.shts.android.keyakifeed.adapters.BindingHolder;
import jp.shts.android.keyakifeed.adapters.FooterRecyclerViewAdapter;
import jp.shts.android.keyakifeed.api.KeyakiFeedApiClient;
import jp.shts.android.keyakifeed.databinding.FragmentMemberImageGridBinding;
import jp.shts.android.keyakifeed.databinding.ListItemImageGridBinding;
import jp.shts.android.keyakifeed.dialogs.DownloadConfirmDialog;
import jp.shts.android.keyakifeed.entities.BlogImage;
import jp.shts.android.keyakifeed.models.Entries;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.services.DownloadImageService;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
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
                        startActivityForResult(PermissionRequireActivity
                                .getDownloadStartIntent(getActivity(), ""), 0);
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
        subscriptions.add(getMemberEntries(member.getId(), counter, PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<BlogImage>>() {
                    @Override
                    public void call(List<BlogImage> blogImages) {
                        if (blogImages == null || blogImages.isEmpty()) {
                            return;
                        }
                        // setup adapter
                        adapter = new MemberImageGridAdapter(getContext(), blogImages);
                        adapter.setOnMaxPageScrollListener(new FooterRecyclerViewAdapter.OnMaxPageScrollListener() {
                            @Override
                            public void onMaxPageScrolled() {
                                if (nowGettingNextEntry) return;
                                nowGettingNextEntry = true;
                                // get next feed
                                getMemberNextEntries();
                            }
                        });
                        binding.recyclerView.setAdapter(adapter);

                    }
                }));
    }

    private void getMemberNextEntries() {
        final Member member = getArguments().getParcelable("member");
        if (member == null) return;
        counter++;
        subscriptions.add(getMemberEntries(member.getId(), (PAGE_LIMIT * counter), PAGE_LIMIT)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        nowGettingNextEntry = false;
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        nowGettingNextEntry = false;
                    }
                })
                .subscribe(new Action1<List<BlogImage>>() {
                    @Override
                    public void call(List<BlogImage> blogImages) {
                        if (blogImages == null || blogImages.isEmpty()) {
                            if (adapter != null) adapter.setFooterVisibility(false);
                        } else {
                            if (adapter != null) {
                                adapter.setFooterVisibility(true);
                                adapter.add(blogImages);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }));
    }

    @NonNull
    private Observable<List<BlogImage>> getMemberEntries(int memberId, int skip, int limit) {
        return KeyakiFeedApiClient.getMemberEntries(memberId, skip, limit)
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Snackbar.make(binding.coordinator,
                                "ブログ記事の取得に失敗しました。通信状態を確認してください。",
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .map(new Func1<Entries, List<BlogImage>>() {
                    @Override
                    public List<BlogImage> call(Entries entries) {
                        List<BlogImage> blogImageArrayList = new ArrayList<>();
                        for (Entry entry : entries) {
                            List<String> imageUrlList = entry.getImageUrlList();
                            for (String imageUrl : imageUrlList) {
                                blogImageArrayList.add(new BlogImage(imageUrl, entry));
                            }
                        }
                        return blogImageArrayList;
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Snackbar.make(binding.coordinator,
                    "ストレージへアクセス許可がない場合は、ダウンロードできません。",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }
        final Member member = getArguments().getParcelable("member");
        if (member == null) {
            return;
        }
        getActivity().startService(
                DownloadImageService.getStartIntent(getActivity(), member.getId()));
    }

    private static class MemberImageGridAdapter
            extends FooterRecyclerViewAdapter<BlogImage, ListItemImageGridBinding> {

        public MemberImageGridAdapter(Context context, List<BlogImage> list) {
            super(context, list);
        }

        @Override
        public BindingHolder<ListItemImageGridBinding>
                onCreateContentItemViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new BindingHolder<>(getContext(), parent, R.layout.list_item_image_grid);
        }

        @Override
        public void onBindContentItemViewHolder(BindingHolder<ListItemImageGridBinding> bindingHolder,
                                                final BlogImage blogImage) {
            final ListItemImageGridBinding binding = bindingHolder.binding;
            binding.setBlogImage(blogImage);
            final View root = binding.getRoot();
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContext().startActivity(
                            GalleryActivity.getStartIntent(
                                    getContext(), (ArrayList<BlogImage>) getList(), getPosition(blogImage)));
                }
            });
        }
    }
}
