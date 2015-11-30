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
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.adapters.MemberDetailFeedListAdapter;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.views.MemberDetailHeader;

// TODO: refresh handle
public class MemberDetailFragment extends Fragment {

    private static final String TAG = MemberDetailFragment.class.getSimpleName();

    public static MemberDetailFragment newMemberDetailFragment(String memberObjectId) {
        Bundle bundle = new Bundle();
        bundle.putString("memberObjectId", memberObjectId);
        MemberDetailFragment memberDetailFragment = new MemberDetailFragment();
        memberDetailFragment.setArguments(bundle);
        return memberDetailFragment;
    }

    private String memberObjectId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        memberObjectId = getArguments().getString("memberObjectId");
    }

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private MemberDetailFeedListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_detail_list, null);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        listView = (ListView) view.findViewById(R.id.feed_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entry entry = (Entry) parent.getItemAtPosition(position);
                Intent intent = BlogActivity.getStartIntent(getActivity(), entry.getObjectId());
                getActivity().startActivity(intent);
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary, R.color.primary, R.color.primary, R.color.primary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // TODO:
                //getAllFeeds();
            }
        });

        Member member = ParseObject.createWithoutData(Member.class, memberObjectId);
        member.fetchIfNeededInBackground(new GetCallback<Member>() {
            @Override
            public void done(Member member, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "failed to get member : id(" + memberObjectId + ")", e);
                    return;
                }
                setupHeader(member);
                setupAdaper(member);
            }
        });
        return view;
    }

    private void setupHeader(Member member) {
        final MemberDetailHeader header = new MemberDetailHeader(getActivity());
        header.setup(member);
        listView.addHeaderView(header, null , false);
    }

    private void setupAdaper(Member member) {
        ParseQuery<Entry> query = Entry.getQuery(30, 0);
        query.whereEqualTo("author_id", member.getObjectId());
        query.findInBackground(new FindCallback<Entry>() {
            @Override
            public void done(List<Entry> entries, ParseException e) {
                if (e != null || entries == null || entries.isEmpty()) {
                    Log.e(TAG, "failed to get member entry", e);
                    return;
                }
                adapter = new MemberDetailFeedListAdapter(getActivity(), entries);
                listView.setAdapter(adapter);
            }
        });
    }
}
