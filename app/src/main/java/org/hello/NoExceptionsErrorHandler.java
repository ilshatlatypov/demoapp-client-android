package org.hello;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Do not throw exceptions on both 4xx and 5xx codes
 * Created by ilshat on 30.07.16.
 */
public class NoExceptionsErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) {
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) {

    }
}
