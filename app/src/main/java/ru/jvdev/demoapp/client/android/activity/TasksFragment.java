package ru.jvdev.demoapp.client.android.activity;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.TasksPageDto;
import ru.jvdev.demoapp.client.android.utils.DateUtils;
import ru.jvdev.demoapp.client.android.utils.HttpCodes;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TasksFragment extends Fragment implements RefreshableFragment {

    private static final int CREATE_REQUEST = 1;
    private static final int DETAILS_REQUEST = 2;
    public static final String EXTRA_TASK_ID = "task_id";

    private ListView tasksListView;
    private FragmentDataLoadingListener listener;

    private User activeUser;

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

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateTaskActivity();
            }
        });

        tasksListView = (ListView) view.findViewById(R.id.tasks_list_view);
        TasksWithSubheadersAdapter adapter = new TasksWithSubheadersAdapter(getActivity());
        tasksListView.setAdapter(adapter);
        tasksListView.setEmptyView(view.findViewById(android.R.id.empty));
        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) parent.getItemAtPosition(position);
                openDetailsActivity(task);
            }
        });

        activeUser = ((DemoApp) getActivity().getApplicationContext()).getActiveUser();
        if (activeUser.getRole() == Role.EMPLOYEE) {
            fab.setVisibility(View.GONE);
        }

        updateTasks();

        return view;
    }

    private void openCreateTaskActivity() {
        Intent intent = new Intent(getActivity(), CreateOrUpdateTaskActivity.class);
        this.startActivityForResult(intent, CREATE_REQUEST);
    }

    private void openDetailsActivity(Task task) {
        Intent intent = new Intent(getActivity(), UserDetailsActivity.class);
        intent.putExtra(EXTRA_TASK_ID, task.getId());
        this.startActivityForResult(intent, DETAILS_REQUEST);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(tasksListView, R.string.prompt_task_saved, Snackbar.LENGTH_SHORT).show();
                updateTasks();
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof FragmentDataLoadingListener) {
            listener = (FragmentDataLoadingListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement FragmentDataLoadingListener");
        }
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

        Call<TasksPageDto> tasksPageDtoCall = activeUser.getRole() == Role.MANAGER ?
                tasksApi.list() : tasksApi.listByUser(activeUser.getUsername());
        tasksPageDtoCall.enqueue(new Callback<TasksPageDto>() {
            @Override
            public void onResponse(Call<TasksPageDto> call, Response<TasksPageDto> response) {
                List<Task> tasks = null;
                if (response.isSuccessful()) {
                    tasks = response.body().getTasks();
                } else if (response.code() == HttpCodes.NOT_FOUND) {
                    tasks = Collections.emptyList();
                }
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
