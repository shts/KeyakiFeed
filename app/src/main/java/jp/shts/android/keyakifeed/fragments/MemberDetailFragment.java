package jp.shts.android.keyakifeed.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.FragmentDetailMemberBinding;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import rx.subscriptions.CompositeSubscription;

public class MemberDetailFragment extends Fragment {

    private static final String TAG = MemberDetailFragment.class.getSimpleName();

//    public static MemberDetailFragment newInstance(String memberObjectId) {
//        MemberDetailFragment memberDetailFragment = new MemberDetailFragment();
//        Bundle bundle = new Bundle();
//        bundle.putString("memberObjectId", memberObjectId);
//        memberDetailFragment.setArguments(bundle);
//        return memberDetailFragment;
//    }

    public static MemberDetailFragment newInstance(Member member) {
        MemberDetailFragment memberDetailFragment = new MemberDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("member", member);
        memberDetailFragment.setArguments(bundle);
        return memberDetailFragment;
    }

    private FragmentDetailMemberBinding binding;
    private int maxScrollSize;
    private boolean isAvatarShown;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @Override
    public void onDestroy() {
        subscriptions.unsubscribe();
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail_member, container, false);

        final Member member = getArguments().getParcelable("member");
        if (member == null) return binding.getRoot();


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Favorite.toggle(member);
            }
        });

        binding.collapsingToolbar.setCollapsedTitleTextColor(
                ContextCompat.getColor(getContext(), android.R.color.white));
        binding.collapsingToolbar.setExpandedTitleColor(
                ContextCompat.getColor(getContext(), android.R.color.transparent));

        ViewPageAdapter adapter = new ViewPageAdapter(
                getActivity().getSupportFragmentManager(), member);
        binding.viewpager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.viewpager);

        binding.appBar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (maxScrollSize == 0)
                    maxScrollSize = appBarLayout.getTotalScrollRange();

                int percentage = (Math.abs(verticalOffset)) * 100 / maxScrollSize;

                if (percentage >= 20 && isAvatarShown) {
                    isAvatarShown = false;
                    binding.viewMemberDetailHeader.hideAnimation();
                }

                if (percentage <= 20 && !isAvatarShown) {
                    isAvatarShown = true;
                    binding.viewMemberDetailHeader.showAnimation();
                }
            }
        });
        maxScrollSize = binding.appBar.getTotalScrollRange();

        //Member.fetch(memberObjectId);
        binding.viewMemberDetailHeader.setup(member);
        binding.collapsingToolbar.setTitle(member.getNameMain());

        return binding.getRoot();
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

    private static class ViewPageAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public ViewPageAdapter(FragmentManager fm, Member member) {
            super(fm);
            MemberEntriesFragment memberEntriesFragment
                    = MemberEntriesFragment.newInstance(member);
            MemberImageGridFragment memberImageGridFragment
                    = MemberImageGridFragment.newInstance(member);
            fragments.add(memberEntriesFragment);
            fragments.add(memberImageGridFragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "ブログ";
            } else if (position == 1) {
                return "画像";
            }
            return super.getPageTitle(position);
        }
    }
}
