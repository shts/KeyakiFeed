package jp.shts.android.keyakifeed.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.activities.BlogActivity;
import jp.shts.android.keyakifeed.activities.MemberDetailActivity;
import jp.shts.android.keyakifeed.models.Entry;
import jp.shts.android.keyakifeed.utils.DateUtils;
import jp.shts.android.keyakifeed.utils.PicassoHelper;

public class FavoriteFeedListAdapter extends RecyclableAdapter<Entry> {

    private static final String TAG = FavoriteFeedListAdapter.class.getSimpleName();

    private final Context context;

    public FavoriteFeedListAdapter(Context context, List list) {
        super(context, list);
        this.context = context;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout labelView;
        ImageView backgroundImageView;
        ImageView profileImageView;
        TextView titleTextView;
        TextView authorTextView;
        TextView updatedTextView;

        public ViewHolder(View view) {
            super(view);
            labelView = (LinearLayout) view.findViewById(R.id.card_rebel);
            titleTextView = (TextView) view.findViewById(R.id.card_title);
            authorTextView = (TextView) view.findViewById(R.id.authorname);
            backgroundImageView = (ImageView) view.findViewById(R.id.card_background);
            profileImageView = (ImageView) view.findViewById(R.id.profile_image);
            updatedTextView = (TextView) view.findViewById(R.id.updated);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Object object) {
        final ViewHolder holder = (ViewHolder) viewHolder;
        final Entry entry = (Entry) object;
        holder.titleTextView.setText(entry.getTitle());
        holder.authorTextView.setText(entry.getAuthor());
        holder.updatedTextView.setText(DateUtils.dateToString(entry.getPublishedDate()));
        List<String> urls = entry.getImageUrlList();
        if (urls != null && !urls.isEmpty()) {
            PicassoHelper.load(context, holder.backgroundImageView, urls.get(0));
        }
        holder.backgroundImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = BlogActivity.getStartIntent(context, entry.getObjectId());
                context.startActivity(intent);
            }
        });
        final String profileImageUrl = entry.getAuthorImageUrl();
        if (!TextUtils.isEmpty(profileImageUrl)) {
            PicassoHelper.loadAndCircleTransform(
                    context, holder.profileImageView, profileImageUrl);
            holder.profileImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = MemberDetailActivity.getStartIntent(context, entry.getAuthorId());
                    context.startActivity(intent);
                }
            });
        } else {
            holder.profileImageView.setImageResource(R.drawable.ic_account_circle_black_48dp);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.list_item_card, viewGroup, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                p.setMargins(8, 8, 8, 8);
                view.requestLayout();
            }
        }
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }
}
