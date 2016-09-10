package ru.jvdev.demoapp.client.android.entity.dto;

import ru.jvdev.demoapp.client.android.entity.Task;

/**
 * Created by ilshat on 28.08.16.
 */
public class TaskDto {

    private int id;
    private String title;

    public TaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
    }

    public Task toTask() {
        Task task = new Task(title);
        task.setId(id);
        return task;
    }
}
