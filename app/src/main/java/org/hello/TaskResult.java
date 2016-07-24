package org.hello;

import java.util.List;

/**
 * Created by ilshat on 24.07.16.
 */
public class TaskResult {
    TaskResultType resultType;
    List<Person> persons;

    public TaskResult(TaskResultType resultType) {
        this.resultType = resultType;
    }

    public TaskResult(List<Person> persons) {
        this.resultType = TaskResultType.SUCCESS;
        this.persons = persons;
    }
}
