package org.hello.activity;

/**
 * Created by ilshat on 22.08.16.
 */
public interface FragmentDataLoadingListener {
    void onDataLoaded();
    void onError(String errorMessage);
}
