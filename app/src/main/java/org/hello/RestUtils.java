package org.hello;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by ilshat on 25.07.16.
 */
public class RestUtils {

    private static final RestTemplate REST_TEMPLATE;
    static {
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.getMessageConverters().add(new StringHttpMessageConverter());
    }

    private RestUtils() {}

    public static ResponseEntity<String> getPersonsList() {
        final String url = "http://192.168.2.11:8080/people";
        return REST_TEMPLATE.exchange(url, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> getPersonDetails(String personUrl) {
        return REST_TEMPLATE.exchange(personUrl, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> deletePerson(String personUrl) {
        return REST_TEMPLATE.exchange(personUrl, HttpMethod.DELETE, null, String.class);
    }

}
