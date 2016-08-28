package org.hello.utils;

import org.hello.Api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestUtils {

    private static final Api.Users usersApi;
    private static final Api.Tasks tasksApi;

    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.102:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        usersApi = retrofit.create(Api.Users.class);
        tasksApi = retrofit.create(Api.Tasks.class);
    }

    private RestUtils() {}

    public static Api.Users getUsersApi() {
        return usersApi;
    }

    public static Api.Tasks getTasksApi() {
        return tasksApi;
    }
}
