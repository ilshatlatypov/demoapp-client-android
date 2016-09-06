package ru.jvdev.demoapp.client.android.utils;

import android.util.Base64;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.jvdev.demoapp.client.android.Api;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestProvider {

    private static Api.Root rootApi;
    private static Api.Users usersApi;
    private static Api.Tasks tasksApi;

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("http://192.168.0.103:8080/")
            .addConverterFactory(GsonConverterFactory.create());

    private RestProvider() {}

    public static void init() {
        initWithCredentials(null, null);
    }

    public static void initWithCredentials(String username, String password) {
        if (username != null && password != null) {
            String credentials = username + ":" + password;
            final String basic =
                    "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();

                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", basic)
                            .header("Accept", "application/json")
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        rootApi = retrofit.create(Api.Root.class);
        usersApi = retrofit.create(Api.Users.class);
        tasksApi = retrofit.create(Api.Tasks.class);
    }

    public static Api.Root getRootApi() { return rootApi; }

    public static Api.Users getUsersApi() {
        return usersApi;
    }

    public static Api.Tasks getTasksApi() {
        return tasksApi;
    }
}
