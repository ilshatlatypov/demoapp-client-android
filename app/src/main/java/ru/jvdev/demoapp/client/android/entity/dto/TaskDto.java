package ru.jvdev.demoapp.client.android.entity.dto;

import com.google.gson.annotations.JsonAdapter;

import java.util.Date;

import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.User;

/**
 * Created by ilshat on 28.08.16.
 */
public class TaskDto {

    private int id;
    private String title;
    private Date date;
    @JsonAdapter(UserDtoTypeAdapterFactory.class)
    private UserDto user;

    public TaskDto(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.date = task.getDate();

        User user = task.getUser();
        if (user != null) {
            this.user = new UserDto(user);
        }
    }

    public Task toTask() {
        Task task = new Task(title, date);
        task.setId(id);
        if (user != null) {
            task.setUser(user.toUser());
        }
        return task;
    }
}
