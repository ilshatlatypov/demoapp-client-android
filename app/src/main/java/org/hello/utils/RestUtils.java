package org.hello.utils;

import org.hello.MyService;
import org.hello.NoExceptionsErrorHandler;
import org.hello.entity.User;
import org.hello.entity.dto.UserDto;
import org.hello.entity.dto.UsersPageDto;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
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
                .baseUrl("http://192.168.0.101:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(MyService.class);

        // REST_TEMPLATE = DigestAuthRestTemplate.getInstance();
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        REST_TEMPLATE.setErrorHandler(new NoExceptionsErrorHandler());
    }

    private static final String USERS_URL = "http://192.168.0.101:8080/users";
    private static final String TASKS_URL = "http://192.168.0.101:8080/tasks";

    private RestUtils() {}

    public static User getUserRetrofit() {
        Response<User> response = null;
        try {
            response = service.getUser(1).execute();
        } catch (IOException e) {
            e.printStackTrace();
            // TODO handle this
        }
        return response.body();
    }

    public static List<User> getUsersListRetrofit() throws IOException {
        Response<UsersPageDto> response = service.listUsers().execute();
        List<UserDto> userDtos = response.body().getUsers();
        List<User> users = new ArrayList<>(userDtos.size());
        for (UserDto userDto : userDtos) {
            users.add(userDto.toUser());
        }
        return users;
    }

    public static ResponseEntity<String> getUsersList() {
        return REST_TEMPLATE.exchange(USERS_URL, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> getUserDetails(String url) {
        return REST_TEMPLATE.exchange(url, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> deleteUser(String url) {
        return REST_TEMPLATE.exchange(url, HttpMethod.DELETE, null, String.class);
    }

    public static ResponseEntity<String> createUser(String userJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);
        return REST_TEMPLATE.exchange(USERS_URL, HttpMethod.POST, entity, String.class);
    }

    public static ResponseEntity<String> getTasksList() {
        return REST_TEMPLATE.exchange(TASKS_URL, HttpMethod.GET, null, String.class);
    }
}
