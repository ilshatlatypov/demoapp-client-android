package org.hello;

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
        REST_TEMPLATE = new RestTemplate();
        REST_TEMPLATE.getMessageConverters().add(new StringHttpMessageConverter(Charset.forName("UTF-8")));
    }

    private static final String PERSONS_URL = "http://192.168.2.11:8080/people";

    private RestUtils() {}

    public static ResponseEntity<String> getPersonsList() {
        return REST_TEMPLATE.exchange(PERSONS_URL, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> getPersonDetails(String personUrl) {
        return REST_TEMPLATE.exchange(personUrl, HttpMethod.GET, null, String.class);
    }

    public static ResponseEntity<String> deletePerson(String personUrl) {
        return REST_TEMPLATE.exchange(personUrl, HttpMethod.DELETE, null, String.class);
    }

    public static ResponseEntity<String> createPerson(String personJson) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(personJson, headers);
        return REST_TEMPLATE.exchange(PERSONS_URL, HttpMethod.POST, entity, String.class);
    }
}
