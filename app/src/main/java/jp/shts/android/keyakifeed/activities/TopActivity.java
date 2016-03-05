package jp.shts.android.keyakifeed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.AllFeedListFragment;
import jp.shts.android.keyakifeed.fragments.AllMemberGridFragment;
import jp.shts.android.keyakifeed.fragments.FavoriteMemberFeedListFragment;
import jp.shts.android.keyakifeed.fragments.MatomeFeedListFragment;
import jp.shts.android.keyakifeed.fragments.OfficialReportListFragment;
import jp.shts.android.keyakifeed.fragments.SettingsFragment;
import jp.shts.android.keyakifeed.utils.PreferencesUtils;

public class TopActivity extends AppCompatActivity {

    private static final String TAG = TopActivity.class.getSimpleName();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        drawerLayout.closeDrawers();
                        final int id = menuItem.getItemId();
                        if (id == getLastSelectedMenuId()) {
                            return false;
                        }
                        setupFragment(id);
                        return false;
                    }
                });
        setupFragment(getLastSelectedMenuId());
    }

    private int getLastSelectedMenuId() {
        return PreferencesUtils.getInt(this, "pre-fragment", R.id.menu_all_feed);
    }

    private void setLastSelectedMenuId(int id) {
        PreferencesUtils.setInt(this, "pre-fragment", id);
    }

    private void setupFragment(int id) {
        Fragment fragment = null;
        switch (id) {
            case R.id.menu_all_feed:
                toolbar.setTitle("すべてのブログ");
                fragment = new AllFeedListFragment();
                break;
            case R.id.menu_fav_member_feed:
                toolbar.setTitle("推しメンのブログ");
                fragment = new FavoriteMemberFeedListFragment();
                break;
            case R.id.menu_report:
                toolbar.setTitle("Official Report");
                fragment = OfficialReportListFragment.newInstance();
                break;
            case R.id.menu_member:
                toolbar.setTitle("すべてのメンバー");
                fragment = AllMemberGridFragment.newInstance(
                        AllMemberGridFragment.ListenerType.START_DETAIL);
                break;
            case R.id.menu_matome:
                toolbar.setTitle("まとめ");
                fragment = new MatomeFeedListFragment();
                break;
            case R.id.menu_settings:
                toolbar.setTitle("設定");
                fragment = new SettingsFragment();
                break;
            case R.id.menu_request:
            case R.id.menu_about_app:
            case R.id.menu_lisences:
                startActivity(OtherMenuActivity.getStartIntent(this, id));
                return;
            default:
                Log.e(TAG, "failed to change fragment");
                return;
        }
        navigationView.getMenu().findItem(id).setChecked(true);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, fragment.toString());
        ft.commit();
        setLastSelectedMenuId(id);
    }
}
