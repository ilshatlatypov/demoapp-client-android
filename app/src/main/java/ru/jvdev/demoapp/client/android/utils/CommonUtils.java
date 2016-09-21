package ru.jvdev.demoapp.client.android.utils;

import android.content.Context;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.activity.DataLoadingListener;

/**
 * Created by ilshat on 21.09.16.
 */

public class CommonUtils {

    private CommonUtils() {}

    public static DataLoadingListener tryCastAsDataLoadingListener(Context context) {
        if (context instanceof DataLoadingListener) {
            return (DataLoadingListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement DataLoadingListener");
        }
    }

    public static String requestFailureMessage(Context ctx, Throwable t) {
        return (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                ctx.getString(R.string.error_server_unavailable) :
                ctx.getString(R.string.error_unknown, t.getMessage());
    }

    public static RestProvider rest(Context ctx) {
        DemoApp app = (DemoApp) ctx.getApplicationContext();
        return app.getRestProvider();
    }
}
