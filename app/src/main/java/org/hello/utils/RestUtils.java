package org.hello.utils;

import org.hello.NoExceptionsErrorHandler;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestUtils {

    private static final RestTemplate REST_TEMPLATE;
    static {
        // REST_TEMPLATE = DigestAuthRestTemplate.getInstance();
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
        REST_TEMPLATE.setErrorHandler(new NoExceptionsErrorHandler());

    }

    private static final String USERS_URL = "http://192.168.0.100:8080/users";

    private RestUtils() {}

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
}
