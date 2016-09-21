package ru.jvdev.demoapp.client.android.utils;

import android.content.Context;

import java.util.Date;
import java.util.List;

import ru.jvdev.demoapp.client.android.entity.Task;

/**
 * Created by ilshat on 21.09.16.
 */

public class TaskUtils {

    private TaskUtils() {}

    public static void addSubheaderTasksByDateGroups(Context ctx, List<Task> tasksSortedByDate) {
        int tasksTotal = tasksSortedByDate.size();
        for (int i = tasksTotal - 1; i >= 0; i--) {
            boolean insertSubheader;
            Date taskDate = tasksSortedByDate.get(i).getDate();
            if (i > 0) {
                Date prevTaskDate = tasksSortedByDate.get(i - 1).getDate();
                insertSubheader = !taskDate.equals(prevTaskDate);
            } else {
                insertSubheader = true;
            }

            if (insertSubheader) {
                String dateAsStr = DateUtils.dateToString(ctx, taskDate);
                tasksSortedByDate.add(i, new Task(0, dateAsStr));
            }
        }
    }
}
