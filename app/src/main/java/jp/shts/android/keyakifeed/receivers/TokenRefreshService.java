package jp.shts.android.keyakifeed.receivers;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class TokenRefreshService extends FirebaseInstanceIdService {

    private static final String TAG = TokenRefreshService.class.getSimpleName();

    @Override
    public void onTokenRefresh() {
        PushRegister.onTokenRefresh(getApplicationContext());
    }
}
