package ru.jvdev.demoapp.client.android.entity.dto;

import com.google.gson.annotations.SerializedName;

import ru.jvdev.demoapp.client.android.entity.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilshat on 28.08.16.
 */
public class TasksPageDto {

    @SerializedName("_embedded")
    private Embedded embedded;

    public List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>(embedded.tasks.size());
        for (TaskDto taskDto : embedded.tasks) {
            tasks.add(taskDto.toTask());
        }
        return tasks;
    }

    public class Embedded {
        private List<TaskDto> tasks = new ArrayList<>();
    }
}
