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

import java.util.Collections;
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
import ru.jvdev.demoapp.client.android.utils.HttpCodes;
import ru.jvdev.demoapp.client.android.utils.TaskUtils;

import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.CREATE;
import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.DETAILS;
import static ru.jvdev.demoapp.client.android.utils.ActivityResultCode.DELETED;
import static ru.jvdev.demoapp.client.android.utils.ActivityResultCode.NEED_PARENT_REFRESH;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.tryCastAsDataLoadingListener;
import static ru.jvdev.demoapp.client.android.utils.IntentExtra.ID;

public class TasksFragment extends Fragment implements RefreshableFragment {

    private Api.Tasks tasksApi;

    private ListView tasksListView;
    private DataLoadingListener listener;

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
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCreateActivity();
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

        tasksApi = rest(getActivity()).getTasksApi();

        updateTasks();

        return view;
    }

    private void openCreateActivity() {
        Intent intent = new Intent(getActivity(), TaskEditActivity.class);
        this.startActivityForResult(intent, CREATE);
    }

    private void openDetailsActivity(Task task) {
        Intent intent = new Intent(getActivity(), TaskDetailsActivity.class);
        intent.putExtra(ID, task.getId());
        this.startActivityForResult(intent, DETAILS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(tasksListView, R.string.prompt_task_saved, Snackbar.LENGTH_SHORT).show();
                updateTasks();
            }
        } else if (requestCode == DETAILS) {
            if (resultCode == DELETED) {
                Snackbar.make(tasksListView, R.string.prompt_task_deleted, Snackbar.LENGTH_SHORT).show();
                updateTasks();
            } else if (resultCode == NEED_PARENT_REFRESH) {
                updateTasks();
            }
        }
    }

    @Override
    public void refreshFragmentData() {
        updateTasks();
    }

    private void updateTasks() {
        Call<TasksPageDto> pageCall = activeUser.getRole() == Role.MANAGER ?
                tasksApi.list() : tasksApi.listByUser(activeUser.getUsername());
        pageCall.enqueue(new Callback<TasksPageDto>() {
            @Override
            public void onResponse(Call<TasksPageDto> call, Response<TasksPageDto> response) {
                List<Task> tasks = null;
                if (response.isSuccessful()) {
                    tasks = response.body().getTasks();
                } else if (response.code() == HttpCodes.NOT_FOUND) {
                    tasks = Collections.emptyList();
                }
                TaskUtils.addSubheaderTasksByDateGroups(getActivity(), tasks);
                putDataToList(tasks);
                listener.onDataLoaded();
            }

            @Override
            public void onFailure(Call<TasksPageDto> call, Throwable t) {
                String message = requestFailureMessage(getActivity(), t);
                listener.onError(message);
            }
        });
    }

    private void putDataToList(List<Task> tasks) {
        TasksWithSubheadersAdapter adapter = (TasksWithSubheadersAdapter) tasksListView.getAdapter();
        adapter.setTasks(tasks);
        adapter.notifyDataSetChanged();
    }

    //region Fragment-Activity attach-detach
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = tryCastAsDataLoadingListener(activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = tryCastAsDataLoadingListener(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
    //endregion
}
