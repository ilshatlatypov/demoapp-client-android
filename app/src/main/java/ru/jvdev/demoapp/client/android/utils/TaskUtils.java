package ru.jvdev.demoapp.client.android.utils;

import android.content.Context;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.jvdev.demoapp.client.android.entity.Task;

/**
 * Created by ilshat on 21.09.16.
 */

public class TaskUtils {

    private TaskUtils() {}

    public static List<Task> addSubheaderTasksByDateGroups(Context ctx, List<Task> tasksSortedByDate) {
        List<Task> tasksWithSubheaders = new ArrayList<>();

        int tasksTotal = tasksSortedByDate.size();
        Date prevTaskDate = null;

        for (int i = 0; i < tasksTotal; i++) {
            Task task = tasksSortedByDate.get(i);
            Date taskDate = task.getDate();

            if (i == 0 || !taskDate.equals(prevTaskDate)) {
                String dateAsStr = DateUtils.dateToString(ctx, taskDate);
                Task subheaderTask = new Task(0, dateAsStr);
                tasksWithSubheaders.add(subheaderTask);
            }

            tasksWithSubheaders.add(task);
            prevTaskDate = taskDate;
        }
        return tasksWithSubheaders;
    }
}
