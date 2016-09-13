package ru.jvdev.demoapp.client.android.activity;


import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.dto.TasksPageDto;
import ru.jvdev.demoapp.client.android.utils.DateUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment implements RefreshableFragment {

    private ListView tasksListView;
    private FragmentDataLoadingListener listener;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        if (tasksListView == null) {
            tasksListView = (ListView) view.findViewById(R.id.tasks_list_view);
            TasksWithSubheadersAdapter adapter = new TasksWithSubheadersAdapter(getActivity());
            tasksListView.setAdapter(adapter);
        }
        updateTasks();

        return view;
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
    public void refreshFragmentData() {
        updateTasks();
    }

    private void updateTasks() {
        DemoApp app = (DemoApp) getActivity().getApplicationContext();
        Api.Tasks tasksApi = app.getRestProvider().getTasksApi();

        Call<TasksPageDto> tasksPageDtoCall = tasksApi.getTasks("date");
        tasksPageDtoCall.enqueue(new Callback<TasksPageDto>() {
            @Override
            public void onResponse(Call<TasksPageDto> call, Response<TasksPageDto> response) {
                List<Task> tasks = response.body().getTasks();
                addSubheaderObjects(tasks);
                putDataToList(tasks);
                listener.onDataLoaded();
            }

            private void addSubheaderObjects(List<Task> tasks) {
                int tasksTotal = tasks.size();
                for (int i = tasksTotal - 1; i >= 0; i--) {
                    boolean insertSubheader;
                    Date taskDate = tasks.get(i).getDate();
                    if (i > 0) {
                        Date prevTaskDate = tasks.get(i - 1).getDate();
                        insertSubheader = !taskDate.equals(prevTaskDate);
                    } else {
                        insertSubheader = true;
                    }

                    if (insertSubheader) {
                        String dateAsStr = DateUtils.dateToString(getActivity(), taskDate);
                        tasks.add(i, new Task(0, dateAsStr));
                    }
                }
            }

            @Override
            public void onFailure(Call<TasksPageDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                listener.onError(message);
            }
        });
    }

    private void putDataToList(List<Task> tasks) {
        TasksWithSubheadersAdapter adapter = (TasksWithSubheadersAdapter) tasksListView.getAdapter();
        adapter.setTasks(tasks);
        adapter.notifyDataSetChanged();
    }
}
