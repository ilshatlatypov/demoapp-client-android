package ru.jvdev.demoapp.client.android;

import android.app.Application;

import ru.jvdev.demoapp.client.android.utils.RestProvider;

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
