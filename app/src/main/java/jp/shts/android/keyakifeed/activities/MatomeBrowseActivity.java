package jp.shts.android.keyakifeed.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.fragments.MatomeBrowseFragment;
import jp.shts.android.keyakifeed.models.Matome;

public class MatomeBrowseActivity extends AppCompatActivity {

    private static final String TAG = MatomeBrowseActivity.class.getSimpleName();

    public static Intent getStartIntent(Context context, Matome matome) {
        Intent intent = new Intent(context, MatomeBrowseActivity.class);
        intent.putExtra("matome", matome);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Matome matome = getIntent().getParcelableExtra("matome");
        if (matome == null || TextUtils.isEmpty(matome.getEntryUrl())) {
            Toast.makeText(getApplicationContext(),
                    "記事の取得に失敗しました", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        MatomeBrowseFragment matomeBrowseFragment
                = MatomeBrowseFragment.newInstance(matome);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.container, matomeBrowseFragment, MatomeBrowseFragment.class.getSimpleName());
        ft.commit();
    }
}
