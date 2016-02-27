package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.ListItemMatomeFeedBinding;
import jp.shts.android.keyakifeed.entities.FeedItem;

public class MatomeFeedListAdapter extends ArrayAdapter<FeedItem> {

    private static final String TAG = MatomeFeedListAdapter.class.getSimpleName();

    private LayoutInflater inflater;

    public MatomeFeedListAdapter(Context context, List<FeedItem> list) {
        super(context, -1, list);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemMatomeFeedBinding binding;

        if (convertView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.list_item_matome_feed, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (ListItemMatomeFeedBinding) convertView.getTag();
        }
        binding.setFeedItem(getItem(position));
        return convertView;
    }
}
