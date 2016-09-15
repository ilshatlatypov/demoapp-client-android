package ru.jvdev.demoapp.client.android.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.User;

/**
 * Created by ilshat on 11.09.16.
 */
public class TasksWithSubheadersAdapter extends BaseAdapter {

    private Context context;
    private List<Task> tasks;

    private static final int ITEM_VIEW_TYPE_SUBHEADER = 0;
    private static final int ITEM_VIEW_TYPE_REGULAR = 1;

    private static final int ITEM_VIEW_TYPE_COUNT = 2;

    public TasksWithSubheadersAdapter(Context context) {
        this.context = context;
        this.tasks = new ArrayList<>();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks.clear();
        this.tasks.addAll(tasks);
    }

    @Override
    public int getCount() {
        return tasks.size();
    }

    @Override
    public Object getItem(int position) {
        return tasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        boolean isSubheader = tasks.get(position).getId() == 0; // if no id - then subheader item
        return isSubheader ? ITEM_VIEW_TYPE_SUBHEADER : ITEM_VIEW_TYPE_REGULAR;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != ITEM_VIEW_TYPE_SUBHEADER;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;

        Task task = tasks.get(position);
        int itemViewType = getItemViewType(position);

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            int layoutId = (itemViewType == ITEM_VIEW_TYPE_SUBHEADER) ?
                    R.layout.list_subheader : R.layout.task_item;
            view = inflater.inflate(layoutId, null);
        } else {
            view = convertView;
        }

        if (itemViewType == ITEM_VIEW_TYPE_SUBHEADER) {
            TextView subheaderView = (TextView) view.findViewById(R.id.subheader);
            subheaderView.setText(task.getTitle());
        } else {
            TextView taskTitleView = (TextView) view.findViewById(R.id.task_title);
            TextView executorView = (TextView) view.findViewById(R.id.executor_name);
            taskTitleView.setText(task.getTitle());

            User user = task.getUser();
            String executorText = user != null ? user.getFullname() : "не назначен";
            executorView.setText("Исполнитель: " + executorText);
        }

        return view;
    }
}
