package ru.jvdev.demoapp.client.android.activity;

/**
 * Created by ilshat on 22.08.16.
 */
public interface DataLoadingListener {
    void onDataLoaded();
    void onError(String errorMessage);
}
