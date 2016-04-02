package jp.shts.android.keyakifeed.activities;

import android.animation.Animator;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.AllFeedListFragment;
import jp.shts.android.keyakifeed.fragments.FavoriteMemberFeedListFragment;
import jp.shts.android.keyakifeed.fragments.MemberEntriesFragment;
import jp.shts.android.keyakifeed.fragments.MemberImageGridFragment;
import jp.shts.android.keyakifeed.utils.PicassoHelper;

public class TopActivity2 extends AppCompatActivity {

    private static final String TAG = TopActivity2.class.getSimpleName();

    private int counter = 1;
    private ViewAnimator viewAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_top);
        // メモリ食い過ぎ
        viewAnimator = (ViewAnimator)
                findViewById(R.id.header).findViewById(R.id.animator);

        Picasso.with(this).load(R.drawable.cover_image_0).fit().centerCrop().into(
                (ImageView) viewAnimator.findViewById(R.id.cover_1)
        );
        Picasso.with(this).load(R.drawable.cover_image_1).fit().centerCrop().into(
                (ImageView) viewAnimator.findViewById(R.id.cover_2)
        );
        Picasso.with(this).load(R.drawable.cover_image_2).fit().centerCrop().into(
                (ImageView) viewAnimator.findViewById(R.id.cover_3)
        );
        Picasso.with(this).load(R.drawable.cover_image_3).fit().centerCrop().into(
                (ImageView) viewAnimator.findViewById(R.id.cover_4)
        );
        Picasso.with(this).load(R.drawable.cover_image_4).fit().centerCrop().into(
                (ImageView) viewAnimator.findViewById(R.id.cover_5)
        );
        Picasso.with(this).load(R.drawable.cover_image_5).fit().centerCrop().into(
                (ImageView) viewAnimator.findViewById(R.id.cover_6)
        );
        runAnimation();

        ViewPageAdapter adapter = new ViewPageAdapter(
                getSupportFragmentManager());
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void runAnimation() {
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewAnimator.setDisplayedChild(counter++);
                if (6 < counter) {
                    counter = 1;
                }
                final View view = viewAnimator.getChildAt(
                        viewAnimator.getDisplayedChild());
                view.animate().scaleX(1.2f).scaleY(1.2f).translationY(30f)
                        .setDuration(5500).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                    }
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.animate().scaleX(1f).scaleY(1f).translationY(0f).setDuration(0)
                                .setListener(null).start();
                    }
                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }
                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }
                }).start();
                runAnimation();
            }
        }, 5 * 1000);
    }


    private static class ViewPageAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragments = new ArrayList<>();

        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
            AllFeedListFragment allFeedListFragment = new AllFeedListFragment();
            FavoriteMemberFeedListFragment favoriteMemberFeedListFragment
                    = new FavoriteMemberFeedListFragment();
            fragments.add(allFeedListFragment);
            fragments.add(favoriteMemberFeedListFragment);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "すべてのブログ";
            } else if (position == 1) {
                return "推しメンのブログ";
            }
            return super.getPageTitle(position);
        }
    }

}
