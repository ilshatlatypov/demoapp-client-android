package org.hello;

import org.springframework.http.ResponseEntity;

/**
 * Created by ilshat on 24.07.16.
 */
public class TaskResult {

    private TaskResultType resultType;
    private Object resultObject;

    public static TaskResult noConnection() {
        return new TaskResult(TaskResultType.NO_CONNECTION);
    }

    public static TaskResult serverUnavailable() {
        return new TaskResult(TaskResultType.SERVER_UNAVAILABLE);
    }

    public static TaskResult ok(ResponseEntity<String> responseEntity) {
        return new TaskResult(responseEntity);
    }

    public TaskResult(TaskResultType resultType) {
        this.resultType = resultType;
    }

    public TaskResult(Object resultObject) {
        this.resultType = TaskResultType.SUCCESS;
        this.resultObject = resultObject;
    }

    public TaskResultType getResultType() {
        return resultType;
    }

    public Object getResultObject() {
        return resultObject;
    }
}
