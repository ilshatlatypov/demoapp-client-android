package ru.jvdev.demoapp.client.android.entity.dto;

import com.google.gson.annotations.SerializedName;

import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.utils.StringUtils;

/**
 * Created by ilshat on 28.08.16.
 */
public class TaskDto {
    private String title;
    @SerializedName("_links")
    private Links links;

    public TaskDto(Task task) {
        this.title = task.getTitle();
    }

    public Task toTask() {
        Task task = new Task(title);
        task.setId(StringUtils.getIdFromURL(links.getSelf().getHref()));
        return task;
    }
}
