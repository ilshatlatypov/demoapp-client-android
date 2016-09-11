package ru.jvdev.demoapp.client.android.entity.dto;

import java.util.Date;

import ru.jvdev.demoapp.client.android.entity.Task;

/**
 * Created by ilshat on 28.08.16.
 */
public class TaskDto {

    private int id;
    private String title;
    private Date date;

    public TaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.date = task.getDate();
    }

    public Task toTask() {
        Task task = new Task(title, date);
        task.setId(id);
        return task;
    }
}
