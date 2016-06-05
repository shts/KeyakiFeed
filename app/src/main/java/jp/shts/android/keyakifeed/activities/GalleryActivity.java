package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.ActivityGalleryBinding;
import jp.shts.android.keyakifeed.entities.BlogImage;
import jp.shts.android.keyakifeed.fragments.GalleryFragment;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = GalleryActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context,
                                        ArrayList<BlogImage> blogImages,
                                        int index) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putParcelableArrayListExtra("blogImages", blogImages);
        intent.putExtra("index", index);
        return intent;
    }

    private ActivityGalleryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_gallery);
        final List<BlogImage> blogImages = getIntent().getParcelableArrayListExtra("blogImages");
        binding.viewPager.setAdapter(new GalleryPagerAdapter(blogImages));
        binding.viewPager.setCurrentItem(getIntent().getIntExtra("index", 0));

        binding.toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // noinspection ConstantConditions
            findViewById(android.R.id.content).setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class GalleryPagerAdapter extends FragmentStatePagerAdapter {

        private final List<BlogImage> blogImageList;

        public GalleryPagerAdapter(List<BlogImage> blogImageList) {
            super(GalleryActivity.this.getSupportFragmentManager());
            this.blogImageList = blogImageList;
        }

        @Override
        public Fragment getItem(int position) {
            return GalleryFragment.newInstance(blogImageList.get(position));
        }

        @Override
        public int getCount() {
            return blogImageList.size();
        }
    }

}
