package jp.shts.android.keyakifeed.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.shts.android.keyakifeed.R;
import jp.shts.android.keyakifeed.databinding.FragmentRequestBinding;

public class RequestFragment extends Fragment {

    private static final String TAG = RequestFragment.class.getSimpleName();

    private FragmentRequestBinding binding;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_request, container, false);

        MovementMethod movementMethod = LinkMovementMethod.getInstance();
        binding.link.setMovementMethod(movementMethod);
        final String link = "お急ぎの場合、<a href=\"https://twitter.com/keyakifeed\">Twitter @keyakifeed </a>へリプライしていただくと返信が早いかもしれません";
        binding.link.setText(Html.fromHtml(link));

        binding.reqSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(createSendRequestIntent(binding.reqEditor.getText().toString()));
            }
        });

        binding.toolbar.setTitle("フィードバック");
        binding.toolbar.setNavigationIcon(R.drawable.ic_clear_white_24dp);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        return binding.getRoot();
    }

    private Intent createSendRequestIntent(String message) {
        return ShareCompat.IntentBuilder.from(getActivity())
                .addEmailTo("keyakifeed@gmail.com")
                .setSubject("KeyakiFeed - フィードバック")
                .setText(getAppVersion() + message)
                .setType("text/plain")
                .getIntent();
    }

    private String getAppVersion() {
        final Context context = getActivity();
        String appPackageName = context.getPackageName();

        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo p = pm.getPackageInfo(appPackageName, 0);

            String appVersionName = p.versionName;
            int appVersionCode = p.versionCode;

            final StringBuilder builder = new StringBuilder();
            builder.append("アプリ情報").append("\n");
            builder.append("=========================").append("\n");
            builder.append("メッセージ").append("\n");
            builder.append("appPackageName=").append(appPackageName).append("\n");
            builder.append("appVersion=").append(appVersionName)
                    .append(" (").append(appVersionCode).append(")\n");
            return builder.toString();

        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException("never reached", e);
        }
    }

}
