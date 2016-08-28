package ru.jvdev.demoapp.client.android.utils;

import ru.jvdev.demoapp.client.android.Api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestProvider {

    private static Api.Users usersApi;
    private static Api.Tasks tasksApi;

    private RestProvider() {}

    public static void init() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.102:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        usersApi = retrofit.create(Api.Users.class);
        tasksApi = retrofit.create(Api.Tasks.class);
    }

    public static Api.Users getUsersApi() {
        return usersApi;
    }

    public static Api.Tasks getTasksApi() {
        return tasksApi;
    }
}
