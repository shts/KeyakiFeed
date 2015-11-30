package jp.shts.android.keyakifeed.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.AllFeedListFragment;
import jp.shts.android.keyakifeed.fragments.AllMemberGridFragment;
import jp.shts.android.keyakifeed.fragments.FavoriteMemberFeedListFragment;
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
                        setup(menuItem.getItemId());
                        return false;
                    }
                });
        setup(getPreFragment());
    }

    private int getPreFragment() {
        return PreferencesUtils.getInt(this, "pre-fragment", R.id.menu_all_feed);
    }

    private void setPreFragment(int id) {
        PreferencesUtils.setInt(this, "pre-fragment", id);
    }

    private void setup(int id) {
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
            case R.id.menu_member:
                toolbar.setTitle("すべてのメンバー");
                fragment = AllMemberGridFragment.newInstance(
                        AllMemberGridFragment.ListenerType.START_DETAIL);
                break;
            default:
                return;
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, fragment, fragment.toString());
        ft.commit();
        setPreFragment(id);
        drawerLayout.closeDrawers();
    }
}
