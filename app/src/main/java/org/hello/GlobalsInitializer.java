package org.hello;

import android.app.Application;

import org.hello.utils.RestProvider;

/**
 * Created by ilshat on 28.08.16.
 */
public class GlobalsInitializer extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        RestProvider.init();
    }
}
