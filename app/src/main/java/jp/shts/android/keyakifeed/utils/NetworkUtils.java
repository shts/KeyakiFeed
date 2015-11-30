package jp.shts.android.keyakifeed.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private NetworkUtils() {}

    /**
     * Check network enable.
     * @param context application context.
     * @return if true network enabled.
     */
    public static boolean enableNetwork(Context context) {
        return NetworkUtils.isConnected(context);
    }

    /**
     * Whether be able to network.
     *
     * @param context
     * @return Return true if network is enable.
     */
    public static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if( ni != null ){
            return cm.getActiveNetworkInfo().isConnected();
        }
        return false;
    }
}
