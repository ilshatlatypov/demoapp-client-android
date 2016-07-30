package org.hello.utils;

import org.hello.security.DigestAuthRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestUtils {

    private static final RestTemplate REST_TEMPLATE;
    static {
        REST_TEMPLATE = DigestAuthRestTemplate.getInstance();
    }

    private static final String USERS_URL = "http://192.168.2.11:8080/users";

    private RestUtils() {}

    public static ResponseEntity<String> getPersonsList() {
        return REST_TEMPLATE.exchange(USERS_URL, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> getPersonDetails(String personUrl) {
        return REST_TEMPLATE.exchange(personUrl, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> deletePerson(String personUrl) {
        return REST_TEMPLATE.exchange(personUrl, HttpMethod.DELETE, null, String.class);
    }

    public static ResponseEntity<String> createUser(String userJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(userJson, headers);
        return REST_TEMPLATE.exchange(USERS_URL, HttpMethod.POST, entity, String.class);
    }
}
