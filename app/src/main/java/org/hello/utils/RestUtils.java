package org.hello.utils;

import org.hello.MyService;
import org.hello.NoExceptionsErrorHandler;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestUtils {

    private static final RestTemplate REST_TEMPLATE;
    private static final MyService service;
    static {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.0.102:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(MyService.class);

        // REST_TEMPLATE = DigestAuthRestTemplate.getInstance();
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        REST_TEMPLATE.setErrorHandler(new NoExceptionsErrorHandler());
    }

    private static final String TASKS_URL = "http://192.168.0.102:8080/tasks";

    private RestUtils() {}

    public static MyService getService() {
        return service;
    }

    public static ResponseEntity<String> getUserDetails(String url) {
        return REST_TEMPLATE.exchange(url, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> deleteUser(String url) {
        return REST_TEMPLATE.exchange(url, HttpMethod.DELETE, null, String.class);
    }

    public static ResponseEntity<String> getTasksList() {
        return REST_TEMPLATE.exchange(TASKS_URL, HttpMethod.GET, null, String.class);
    }
}
