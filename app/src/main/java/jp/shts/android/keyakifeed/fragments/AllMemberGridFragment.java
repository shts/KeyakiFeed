package jp.shts.android.keyakifeed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.squareup.otto.Subscribe;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.AllMemberGridListAdapter;
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

    private GridView gridView;
    private AllMemberGridListAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final AdapterView.OnItemClickListener memberChooserListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Member member = (Member) gridView.getItemAtPosition(position);
            Favorite.toggle(member.getObjectId());
            adapter.notifyDataSetChanged();
        }
    };
    private final AdapterView.OnItemClickListener startDetailListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Member member = (Member) gridView.getItemAtPosition(position);
            Intent intent = MemberDetailActivity.getStartIntent(getContext(), member.getObjectId());
            getContext().startActivity(intent);
        }
    };

    private String listenerType;
    private static List<Member> cache;

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
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // clear cache
                cache = null;
                Member.all(Member.getQuery());
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        gridView = (GridView) view.findViewById(R.id.gridview);
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
            gridView.setOnItemClickListener(memberChooserListener);
        } else if (listenerType.equals(ListenerType.START_DETAIL.name())) {
            toolbar.setVisibility(View.GONE);
            gridView.setOnItemClickListener(startDetailListener);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Member.all(Member.getQuery());
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
            cache = callback.members;
            adapter = new AllMemberGridListAdapter(getContext(), callback.members);
            gridView.setAdapter(adapter);
        }
    }
}
