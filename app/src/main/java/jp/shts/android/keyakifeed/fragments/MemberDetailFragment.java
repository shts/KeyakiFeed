package jp.shts.android.keyakifeed.fragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class MemberDetailFragment extends Fragment {

    private static final String TAG = MemberDetailFragment.class.getSimpleName();

    public static MemberDetailFragment newInstance(String memberObjectId) {
        MemberDetailFragment memberDetailFragment = new MemberDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("memberObjectId", memberObjectId);
        memberDetailFragment.setArguments(bundle);
        return memberDetailFragment;
    }

    private FragmentDetailMemberBinding binding;
    private String memberObjectId;

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

        ViewPageAdapter adapter = new ViewPageAdapter(
                getActivity().getSupportFragmentManager(), memberObjectId);
        binding.viewpager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.viewpager);

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

        public ViewPageAdapter(FragmentManager fm, String memberObjectId) {
            super(fm);
            MemberEntriesFragment memberEntriesFragment
                    = MemberEntriesFragment.newInstance(memberObjectId);
            MemberImageGridFragment memberImageGridFragment
                    = MemberImageGridFragment.newInstance(memberObjectId);
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
