package jp.shts.android.keyakifeed.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.models.Favorite;
import jp.shts.android.keyakifeed.models2.Member;
import jp.shts.android.keyakifeed.models.eventbus.BusHolder;
import jp.shts.android.keyakifeed.utils.PicassoHelper;

public class MemberDetailHeader extends RelativeLayout {

    private static final String TAG = MemberDetailHeader.class.getSimpleName();

    private ImageView profileImageView;
    private ImageView favoriteIconImageView;

    private TextView nameMainTextView;
    private TextView nameSubTextView;

    private TextView birthdayTextView;
    private TextView birthplaceTextView;
    private TextView bloodTypeTextView;
    private TextView constellationTextView;
    private TextView heightTextView;

    public MemberDetailHeader(Context context) {
        this(context, null);
    }
    public MemberDetailHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MemberDetailHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        BusHolder.get().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        BusHolder.get().unregister(this);
        super.onDetachedFromWindow();
    }

    private void init() {
        final View rootView = LayoutInflater.from(getContext()).inflate(R.layout.view_member_detail, this);
        profileImageView = (ImageView) rootView.findViewById(R.id.profile_image);
        favoriteIconImageView = (ImageView) rootView.findViewById(R.id.favorite_icon);
        nameMainTextView = (TextView) rootView.findViewById(R.id.name_main);
        nameSubTextView = (TextView) rootView.findViewById(R.id.name_sub);
        birthdayTextView = (TextView) rootView.findViewById(R.id.birthday);
        birthplaceTextView = (TextView) rootView.findViewById(R.id.birthplace);
        bloodTypeTextView = (TextView) rootView.findViewById(R.id.blood_type);
        constellationTextView = (TextView) rootView.findViewById(R.id.constellation);
        heightTextView = (TextView) rootView.findViewById(R.id.height);
    }

    public void setup(Member member) {
        // そのほかメンバーのブログの場合
        final Context context = getContext();
        PicassoHelper.loadAndCircleTransform(
                context, profileImageView, member.getImageUrl());

        nameMainTextView.setText(member.getNameMain());
        nameSubTextView.setText(member.getNameSub());

        final Resources res = context.getResources();
        birthdayTextView.setText(res.getString(R.string.property_name_birthday, member.getBirthday()));
        birthplaceTextView.setText(res.getString(R.string.property_name_birthplace, member.getBirthplace()));
        bloodTypeTextView.setText(res.getString(R.string.property_name_blood_type, member.getBloodType()));
        constellationTextView.setText(res.getString(R.string.property_name_constellation, member.getConstellation()));
        heightTextView.setText(res.getString(R.string.property_name_height, member.getHeight()));

        if (Favorite.exist(member)) {
            favoriteIconImageView.setVisibility(View.VISIBLE);
        } else {
            favoriteIconImageView.setVisibility(View.GONE);
        }
    }

    public void showAnimation() {
        profileImageView.animate()
                .scaleY(1).scaleX(1)
                .start();
        favoriteIconImageView.animate()
                .scaleY(1).scaleX(1)
                .start();
    }

    public void hideAnimation() {
        profileImageView.animate().scaleY(0).scaleX(0).setDuration(200).start();
        favoriteIconImageView.animate().scaleY(0).scaleX(0).setDuration(200).start();
    }

    @Subscribe
    public void onChangedFavoriteState(Favorite.ChangedFavoriteState state) {
        if (state.e == null) {
            if (state.action == Favorite.ChangedFavoriteState.Action.ADD) {
                favoriteIconImageView.setVisibility(View.VISIBLE);
            } else {
                favoriteIconImageView.setVisibility(View.GONE);
            }
        }
    }

}
