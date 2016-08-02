package org.hello;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/**
 * Created by ilshat on 30.07.16.
 */
public class MyResponseErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) {
        return false;
    }

    @Override
    public void handleError(ClientHttpResponse response) {

    }
}
