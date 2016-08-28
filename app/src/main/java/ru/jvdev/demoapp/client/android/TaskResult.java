package ru.jvdev.demoapp.client.android;

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

    public static TaskResult unexpectedResponse(int responseCode) {
        TaskResult taskResult = new TaskResult();
        taskResult.resultType = TaskResultType.UNEXPECTED_RESPONSE_CODE;
        taskResult.resultObject = responseCode;
        return taskResult;
    }

    public static TaskResult ok(ResponseEntity<String> responseEntity) {
        TaskResult taskResult = new TaskResult();
        taskResult.resultType = TaskResultType.SUCCESS;
        taskResult.resultObject = responseEntity;
        return taskResult;
    }

    public TaskResult() {}

    public TaskResult(TaskResultType resultType) {
        this.resultType = resultType;
    }

    public TaskResultType getResultType() {
        return resultType;
    }

    public Object getResultObject() {
        return resultObject;
    }
}
