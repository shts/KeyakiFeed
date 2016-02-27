package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.databinding.ListItemEntryBinding;
import jp.shts.android.keyakifeed.models.Entry;

public class AllFeedListAdapter extends ArrayAdapter<Entry> {

    private static final String TAG = AllFeedListAdapter.class.getSimpleName();

    private OnPageMaxScrolledListener pageMaxScrolledListener;
    private LayoutInflater inflater;

    public AllFeedListAdapter(Context context, List<Entry> list) {
        super(context, -1, list);
        inflater = LayoutInflater.from(context);
    }

    public interface OnPageMaxScrolledListener {
        void onScrolledMaxPage();
    }

    public void setPageMaxScrolledListener(OnPageMaxScrolledListener listener) {
        pageMaxScrolledListener = listener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemEntryBinding binding;

        if (convertView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.list_item_entry, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ListItemEntryBinding) convertView.getTag();
        }

        final Entry entry = getItem(position);
        binding.setEntry(entry);
        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = getContext();
                context.startActivity(
                        MemberDetailActivity.getStartIntent(context, entry.getAuthorId()));
            }
        });

        if (getCount() - 1 <= position) {
            if (pageMaxScrolledListener != null) {
                pageMaxScrolledListener.onScrolledMaxPage();
            }
        }
        return convertView;
    }
}
