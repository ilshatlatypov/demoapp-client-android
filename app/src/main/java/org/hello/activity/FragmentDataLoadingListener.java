package org.hello.activity;

import org.hello.TaskResultType;

/**
 * Created by ilshat on 22.08.16.
 */
public interface FragmentDataLoadingListener {
    void onDataLoaded();
    void onError(TaskResultType resultType);
}
