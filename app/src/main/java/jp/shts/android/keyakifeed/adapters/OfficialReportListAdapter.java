package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.databinding.ListItemReportBinding;
import jp.shts.android.keyakifeed.entities.Blog;
import jp.shts.android.keyakifeed.models.Report;

public class OfficialReportListAdapter extends ArrayRecyclerAdapter<Report, BindingHolder<ListItemReportBinding>> {

    private static final String TAG = OfficialReportListAdapter.class.getSimpleName();

    public OfficialReportListAdapter(@NonNull Context context) {
        super(context);
    }

    @Override
    public BindingHolder<ListItemReportBinding> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new BindingHolder<>(getContext(), parent, R.layout.list_item_report);
    }

    @Override
    public void onBindViewHolder(BindingHolder<ListItemReportBinding> holder, int position) {
        final Report report = getItem(position);
        ListItemReportBinding binding = holder.binding;
        binding.setReport(report);

        final View root = binding.getRoot();
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().startActivity(
                        BlogActivity.getStartIntent(getContext(), new Blog(report)));
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (root.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) root.getLayoutParams();
                p.setMargins(8, 8, 8, 8);
                root.requestLayout();
            }
        }
    }
}
