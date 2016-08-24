package org.hello.activity;

import org.springframework.http.ResponseEntity;

/**
 * Created by ilshat on 23.08.16.
 */
public class TaskResultNew {

    private ResponseEntity<String> responseEntity;
    private ErrorType errorType;

    public TaskResultNew(ResponseEntity<String> responseEntity) {
        this.responseEntity = responseEntity;
    }

    public TaskResultNew(ErrorType errorType) {
        this.errorType = errorType;
    }

    public ResponseEntity<String> getResponseEntity() {
        return responseEntity;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public boolean isError() {
        return errorType != null;
    }
}
