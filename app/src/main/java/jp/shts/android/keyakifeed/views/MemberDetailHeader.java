package jp.shts.android.keyakifeed.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.models.Member;
import jp.shts.android.keyakifeed.utils.PicassoHelper;

public class MemberDetailHeader extends RelativeLayout {

    private static final String TAG = MemberDetailHeader.class.getSimpleName();

    private ImageView profileImageView;

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

    private void init() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.view_member_detail, this);
        profileImageView = (ImageView) root.findViewById(R.id.profile_image);
        nameMainTextView = (TextView) root.findViewById(R.id.name_main);
        nameSubTextView = (TextView) root.findViewById(R.id.name_sub);
        birthdayTextView = (TextView) root.findViewById(R.id.birthday);
        birthplaceTextView = (TextView) root.findViewById(R.id.birthplace);
        bloodTypeTextView = (TextView) root.findViewById(R.id.blood_type);
        constellationTextView = (TextView) root.findViewById(R.id.constellation);
        heightTextView = (TextView) root.findViewById(R.id.height);

        int height = (int) ( /*240*/ 280 * getContext().getResources().getDisplayMetrics().density);
        root.setLayoutParams(new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height));
    }

    public void setup(Member member) {
        final Context context = getContext();
        PicassoHelper.loadAndCircleTransform(
                context, profileImageView, member.getProfileImageUrl());

        nameMainTextView.setText(member.getNameMain());
        nameSubTextView.setText(member.getNameSub());

        final Resources res = context.getResources();
        birthdayTextView.setText(res.getString(R.string.property_name_birthday, member.getBirthday()));
        birthplaceTextView.setText(res.getString(R.string.property_name_birthplace, member.getBirthPlace()));
        bloodTypeTextView.setText(res.getString(R.string.property_name_blood_type, member.getBloodType()));
        constellationTextView.setText(res.getString(R.string.property_name_constellation, member.getConstellation()));
        heightTextView.setText(res.getString(R.string.property_name_height, member.getHeight()));
    }

}
