package jp.shts.android.keyakifeed.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import jp.shts.android.keyakifeed.R;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    /** ブログ更新通知可否設定 */
    private static final String NOTIFICATION_ENABLE = "pref_key_blog_updated_notification_enable";
    /** ブログ更新通知制限設定(お気に入りメンバーのみ通知する設定) */
    private static final String NOTIFICATION_RESTRICTION_ENABLE = "pref_key_blog_updated_notification_restriction_enable";
    /** 「すべてのブログ」画面の未読のブログ記事をマークする設定 */
    public static final String MARK_UNREAD_ARTICLES = "pref_key_mark_unread_articles";
    /** 「推しメンのブログ」画面の未読のブログ記事をマークする設定 */
    public static final String MARK_UNREAD_ARTICLES_ONLY_FAVORITE = "pref_key_mark_unread_articles_only_favorite";

    private SharedPreferences.OnSharedPreferenceChangeListener listener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            updateView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateView();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
    }

    private void updateView() {
        // 通知設定
        CheckBoxPreference enableNotification
                = (CheckBoxPreference) findPreference(NOTIFICATION_ENABLE);
        CheckBoxPreference restrictionNotification
                = (CheckBoxPreference) findPreference(NOTIFICATION_RESTRICTION_ENABLE);
        restrictionNotification.setEnabled(enableNotification.isChecked());
    }

}
