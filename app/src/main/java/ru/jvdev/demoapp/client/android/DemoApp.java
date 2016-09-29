package ru.jvdev.demoapp.client.android;

import android.app.Application;

import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.utils.RestProvider;

/**
 * Created by ilshat on 28.08.16.
 */
public class DemoApp extends Application {

    private RestProvider restProvider;
    private User activeUser;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void setActiveUser(User user) {
        this.activeUser = user;
    }

    public void setRestCredentials(String username, String password) {
        if (username != null && password != null) {
            restProvider = new RestProvider(username, password);
        } else {
            restProvider = new RestProvider();
        }
    }

    public User getActiveUser() {
        return activeUser;
    }

    public RestProvider getRestProvider() {
        return restProvider;
    }
}
