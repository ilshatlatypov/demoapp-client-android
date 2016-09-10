package ru.jvdev.demoapp.client.android.utils;

import android.util.Base64;

import org.springframework.http.MediaType;

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

    private Api.Users usersApi;
    private Api.Tasks tasksApi;

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl("http://192.168.0.102:8080/")
            .addConverterFactory(GsonConverterFactory.create());

    public RestProvider() {
        this(null, null);
    }

    public RestProvider(String username, String password) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        if (username != null && password != null) {
            setBasicAuthCredentials(httpClient, username, password);
        }

        OkHttpClient client = httpClient.build();
        Retrofit retrofit = builder.client(client).build();
        usersApi = retrofit.create(Api.Users.class);
        tasksApi = retrofit.create(Api.Tasks.class);
    }

    private void setBasicAuthCredentials(OkHttpClient.Builder httpClient,
                                         String username, String password) {
        String credentials = username + ":" + password;
        final String basic =
                "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
    }

    public Api.Users getUsersApi() {
        return usersApi;
    }

    public Api.Tasks getTasksApi() {
        return tasksApi;
    }
}
