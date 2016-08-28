package org.hello.utils;

import org.hello.MyService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestUtils {

    private static final MyService service;
    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.102:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(MyService.class);
    }

    private RestUtils() {}

    public static MyService getService() {
        return service;
    }
}
