package org.hello.activity;


import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.hello.R;
import org.hello.TaskResult;
import org.hello.TaskResultType;
import org.hello.entity.Task;
import org.hello.utils.ConnectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment {

    private Context context;
    private ListView tasksListView;

    private FragmentDataLoadingListener listener;
    private UpdateTasksTask updateTasksTask;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentDataLoadingListener) {
            listener = (FragmentDataLoadingListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement FragmentDataLoadingListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        context = view.getContext();

        if (tasksListView == null) {
            tasksListView = (ListView) view.findViewById(R.id.tasks_list_view);
            ArrayAdapter<Task> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
            tasksListView.setAdapter(adapter);
        }
        updateTasks();

        return view;
    }

    private void updateTasks() {
        if (updateTasksTask != null) {
            return;
        }
        updateTasksTask = new UpdateTasksTask();
        updateTasksTask.execute();
    }

    private class UpdateTasksTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(context)) {
                return TaskResult.noConnection();
            }

            try {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return TaskResult.ok(null);
            } catch (ResourceAccessException e) {
                return TaskResult.serverUnavailable();
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            updateTasksTask = null;

            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                putDataToList(null);
                listener.onDataLoaded();
                return;
//                ResponseEntity<String> responseEntity = (ResponseEntity<String>) taskResult.getResultObject();
//                if (responseEntity.getStatusCode() == HttpStatus.OK) {
//                    putDataToList(responseEntity);
//                    listener.onDataLoaded();
//                    return;
//                }
            }
            listener.onError(taskResult.getResultType());
        }
    }

    private void putDataToList(ResponseEntity<String> responseEntity) {
//        List<User> users = null;
//        try {
//            users = JSONUtils.parseAsUsersList(responseEntity.getBody());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        ArrayAdapter<Task> adapter = (ArrayAdapter<Task>) tasksListView.getAdapter();
        adapter.add(new Task("Fix the issue"));
        adapter.add(new Task("Deploy build"));
        adapter.add(new Task("Install application"));
        //adapter.clear();
        //adapter.addAll(users);
        adapter.notifyDataSetChanged();
    }

}
