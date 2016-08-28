package ru.jvdev.demoapp.client.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by ilshat on 25.07.16.
 */
public class ConnectionUtils {

    private ConnectionUtils() {}

    public static boolean isConnected(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
