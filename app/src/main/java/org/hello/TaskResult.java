package org.hello;

import java.util.List;

/**
 * Created by ilshat on 24.07.16.
 */
public class TaskResult {

    private TaskResultType resultType;
    private Object resultObject;

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
