package jp.shts.android.keyakifeed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.AllMemberGridListAdapter;
import jp.shts.android.keyakifeed.adapters.ArrayRecyclerAdapter;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;

public class AllMemberGridFragment extends Fragment {

    private static final String TAG = AllMemberGridFragment.class.getSimpleName();

    public enum ListenerType {
        MEMBER_CHOOSER, START_DETAIL;
    }

    public static AllMemberGridFragment newInstance(ListenerType listenerType) {
        Bundle bundle = new Bundle();
        bundle.putString("listenerType", listenerType.name());
        AllMemberGridFragment allMemberGridFragment = new AllMemberGridFragment();
        allMemberGridFragment.setArguments(bundle);
        return allMemberGridFragment;
    }

    private AllMemberGridListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String listenerType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listenerType = getArguments().getString("listenerType");
        BusHolder.get().register(this);
    }

    @Override
    public void onDestroy() {
        BusHolder.get().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_member_grid, null);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Member.all(Member.getQuery());
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
            }
        });

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        adapter = new AllMemberGridListAdapter(getContext());
        adapter.setOnMemberClickListener(new AllMemberGridListAdapter.OnMemberClickListener() {
            @Override
            public void onClick(Member member) {
                if (listenerType.equals(ListenerType.MEMBER_CHOOSER.name())) {
                    Favorite.toggle(member.getObjectId());
                    adapter.notifyDataSetChanged();

                } else {
                    Intent intent = MemberDetailActivity.getStartIntent(
                            getContext(), member.getObjectId());
                    getContext().startActivity(intent);
                }
            }
        });
        recyclerView.setAdapter(adapter);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (listenerType.equals(ListenerType.MEMBER_CHOOSER.name())) {
            toolbar.setVisibility(View.VISIBLE);
            toolbar.setTitleTextColor(getResources().getColor(R.color.primary));
            toolbar.setTitle("推しメンを選択してください");
            toolbar.setNavigationIcon(R.drawable.ic_clear_green_500_18dp);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        } else if (listenerType.equals(ListenerType.START_DETAIL.name())) {
            toolbar.setVisibility(View.GONE);
        }

        Member.all(Member.getQuery());
        return view;
    }

    @Subscribe
    public void onGotMembers(Member.GetMembersCallback callback) {
        if (swipeRefreshLayout != null) {
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        if (callback.e != null || callback.members == null || callback.members.isEmpty()) {
            Log.e(TAG, "cannot get members", callback.e);
        } else {
            adapter.reset(callback.members);
        }
    }
}
