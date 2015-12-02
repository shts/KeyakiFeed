package jp.shts.android.keyakifeed.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.parse.FindCallback;
import com.parse.ParseException;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.AllMemberActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.adapters.AllMemberGridListAdapter;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models.Member;

public class AllMemberGridFragment extends Fragment {

    private static final String TAG = AllMemberGridFragment.class.getSimpleName();

    public enum ListenerType {
        MEMBER_CHOOSER, START_DETAIL;
    }

    private Toolbar toolbar;
    private GridView gridView;
    private AllMemberGridListAdapter adapter;

    private final AdapterView.OnItemClickListener memberChooserListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Member member = (Member) gridView.getItemAtPosition(position);
            Favorite.toggle(member.getObjectId());
            adapter.notifyDataSetChanged();
        }
    };
    private final AdapterView.OnItemClickListener startDetailListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Member member = (Member) gridView.getItemAtPosition(position);
            Intent intent = MemberDetailActivity.getStartIntent(getContext(), member.getObjectId());
            getContext().startActivity(intent);
        }
    };

    public static AllMemberGridFragment newInstance(ListenerType listenerType) {
        Bundle bundle = new Bundle();
        bundle.putString("listenerType", listenerType.name());
        AllMemberGridFragment allMemberGridFragment = new AllMemberGridFragment();
        allMemberGridFragment.setArguments(bundle);
        return allMemberGridFragment;
    }

    private String listenerType;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listenerType = getArguments().getString("listenerType");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_member_grid, null);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
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
        setupAdapter();
    }

    private void setupAdapter() {
        Member.getQuery().findInBackground(new FindCallback<Member>() {
            @Override
            public void done(List<Member> members, ParseException e) {
                if (e != null || members == null || members.isEmpty()) {
                    Log.e(TAG, "cannot get members", e);
                } else {
                    adapter = new AllMemberGridListAdapter(getContext(), members);
                    gridView.setAdapter(adapter);
                }
            }
        });
    }
}
