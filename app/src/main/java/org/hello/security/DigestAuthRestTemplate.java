package org.hello.security;

import android.support.annotation.NonNull;

import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.hello.utils.StringConstants.COMMA;
import static org.hello.utils.StringConstants.COMMA_SPACE;
import static org.hello.utils.StringConstants.EMPTY;
import static org.hello.utils.StringConstants.EQUAL;
import static org.hello.utils.StringConstants.QUOTE;
import static org.hello.utils.StringConstants.SLASH;

/**
 * Created by ilshat on 30.07.16.
 */
public class DigestAuthRestTemplate extends RestTemplate {

    private String login;
    private String password;

    public DigestAuthRestTemplate(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public <T> ResponseEntity<T> exchange(String url, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables) throws RestClientException {
        ResponseEntity<T> responseEntity = super.exchange(url, method, requestEntity, responseType, uriVariables);
        if (responseEntity.getStatusCode() != HttpStatus.UNAUTHORIZED) {
            return responseEntity;
        }

        Map<String, String> authParams = getAuthParams(responseEntity);
        String relativeUrl = getRelativeUrl(url);

        HttpHeaders existingHeaders = new HttpHeaders();
        Object existingEntity = null;
        if (requestEntity != null) {
            existingHeaders = requestEntity.getHeaders();
            existingEntity = requestEntity.getBody();
        }

        HttpAuthentication auth = new HttpDigestAuthentication(login, password, authParams, method, relativeUrl);
        existingHeaders.setAuthorization(auth);

        HttpEntity<?> entity = new HttpEntity<>(existingEntity, existingHeaders);
        return super.exchange(url, method, entity, responseType, uriVariables);
    }

    @NonNull
    private static <T> Map<String, String> getAuthParams(ResponseEntity<T> responseEntity) {
        Map<String, String> authParams = new HashMap<>();
        String wwwAuthHeader = responseEntity.getHeaders().get("WWW-Authenticate").get(0);
        String[] pieces = wwwAuthHeader.substring("Digest ".length()).split(COMMA_SPACE);
        for (String piece : pieces) {
            if (piece.contains(EQUAL)) {
                String[] authParamParts = piece.split(EQUAL);
                String key = authParamParts[0];
                String value = authParamParts[1].replace(QUOTE, EMPTY).replace(COMMA, EMPTY);
                authParams.put(key, value);
            }
        }
        return authParams;
    }

    @NonNull
    private static String getRelativeUrl(String url) {
        int slashPos = url.indexOf(SLASH, "https://".length());
        return slashPos != -1 ? url.substring(slashPos) : SLASH;
    }
}