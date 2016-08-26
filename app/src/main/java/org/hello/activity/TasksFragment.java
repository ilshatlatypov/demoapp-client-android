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
import org.hello.entity.Task;
import org.hello.utils.ConnectionUtils;
import org.hello.utils.JSONUtils;
import org.hello.utils.RestUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

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

    private class UpdateTasksTask extends AsyncTask<Void, Void, TaskResultNew> {

        @Override
        protected TaskResultNew doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(context)) {
                return new TaskResultNew(ErrorType.NO_CONNECTION);
            }

            try {
                return new TaskResultNew(RestUtils.getTasksList());
            } catch (ResourceAccessException e) {
                return new TaskResultNew(ErrorType.SERVER_UNAVAILABLE);
            }
        }

        @Override
        protected void onPostExecute(TaskResultNew taskResult) {
            updateTasksTask = null;

            if (taskResult.isError()) {
                listener.onError("error");
                return;
            }

            ResponseEntity<String> responseEntity = taskResult.getResponseEntity();
            HttpStatus statusCode = responseEntity.getStatusCode();
            if (statusCode == HttpStatus.OK) {
                putDataToList(responseEntity);
                listener.onDataLoaded();
            } else {
                listener.onError("unexpected response");
            }
        }
    }

    private void putDataToList(ResponseEntity<String> responseEntity) {
        List<Task> tasks = null;
        try {
            tasks = JSONUtils.parseAsTasksList(responseEntity.getBody());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<Task> adapter = (ArrayAdapter<Task>) tasksListView.getAdapter();
        adapter.clear();
        adapter.addAll(tasks);
        adapter.notifyDataSetChanged();
    }
}
