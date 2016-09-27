package ru.jvdev.demoapp.client.android.activity.task;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.activity.RefreshableFragment;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.TaskDto;
import ru.jvdev.demoapp.client.android.entity.dto.TasksPageDto;
import ru.jvdev.demoapp.client.android.utils.HttpCodes;
import ru.jvdev.demoapp.client.android.utils.TaskUtils;

import static ru.jvdev.demoapp.client.android.activity.utils.ActivityRequestCode.CREATE;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityRequestCode.DETAILS;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode.DELETED;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode.DONE_STATE_CHANGED;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode.NEED_PARENT_REFRESH;
import static ru.jvdev.demoapp.client.android.activity.utils.IntentExtra.DONE;
import static ru.jvdev.demoapp.client.android.activity.utils.IntentExtra.ID;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;

public class TasksFragment extends Fragment implements RefreshableFragment {

    private static final String ARG_FILTER = "filter";

    public static final int TASKS_ALL_DONE = 0;
    public static final int TASKS_ALL_TODO = 1;
    public static final int TASKS_CURRENT_DONE = 2;
    public static final int TASKS_CURRENT_TODO = 3;

    private ViewSwitcher viewSwitcher;
    private ListView tasksListView;

    private Api.Tasks tasksApi;
    private User currentUser;
    private int filter;

    public TasksFragment() {
        // Required empty public constructor
    }

    public static TasksFragment newInstance(int filter) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FILTER, filter);
        fragment.setArguments(args);
        return fragment;
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
        adapter.setTasks(Arrays.asList(new Task()));
        tasksListView.setAdapter(adapter);
        tasksListView.setEmptyView(view.findViewById(android.R.id.empty));
        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = (Task) parent.getItemAtPosition(position);
                openDetailsActivity(task);
            }
        });

        currentUser = ((DemoApp) getActivity().getApplicationContext()).getActiveUser();
        if (currentUser.getRole() == Role.EMPLOYEE) {
            fab.setVisibility(View.GONE);
        }

        viewSwitcher = new ViewSwitcher(getActivity(), view, R.id.progress_bar, R.id.tasks_list_view, R.id.error_layout);
        Button retryButton = (Button) view.findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTasks();
            }
        });

        filter = getArguments().getInt(ARG_FILTER);
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
            } else if (resultCode == DONE_STATE_CHANGED) {
                final int taskId = data.getIntExtra(ID, 0);
                final boolean actualDoneState = data.getBooleanExtra(DONE, false);
                int messageResId = actualDoneState ? R.string.prompt_task_marked_as_done : R.string.prompt_task_marked_as_not_done;
                Snackbar.make(tasksListView, messageResId, Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_cancel, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                revertTaskDoneState(taskId, actualDoneState);
                            }
                        }).show();
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
        viewSwitcher.showProgressBar();

        String username = currentUser.getUsername();
        Call<TasksPageDto> pageCall;
        if (filter == TASKS_ALL_DONE) {
            pageCall = tasksApi.listDone();
        } else if (filter == TASKS_ALL_TODO) {
            pageCall = tasksApi.listNotDone();
        } else if (filter == TASKS_CURRENT_DONE) {
            pageCall = tasksApi.listDoneByUser(username);
        } else { // filter == TASKS_CURRENT_TODO
            pageCall = tasksApi.listNotDoneByUser(username);
        }

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
                viewSwitcher.showMainLayout();
            }

            @Override
            public void onFailure(Call<TasksPageDto> call, Throwable t) {
                String message = requestFailureMessage(getActivity(), t);
                showError(message);
            }
        });
    }

    private void putDataToList(List<Task> tasks) {
        TasksWithSubheadersAdapter adapter = (TasksWithSubheadersAdapter) tasksListView.getAdapter();
        adapter.setTasks(tasks);
        adapter.notifyDataSetChanged();
    }

    private void showError(String errorMessage) {
        TextView errorTextView = (TextView) getActivity().findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }

    private void revertTaskDoneState(final int taskId, final boolean actualDoneState) {
        Snackbar.make(tasksListView, R.string.prompt_sending_request, Snackbar.LENGTH_INDEFINITE).show();

        final boolean revertedDoneState = !actualDoneState;
        Task task = new Task();
        task.setDone(revertedDoneState);

        Call<Void> deleteUserCall = tasksApi.patch(taskId, new TaskDto(task));
        deleteUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    int messageResId = revertedDoneState ? R.string.prompt_task_undone_canceled : R.string.prompt_task_done_canceled;
                    Snackbar.make(tasksListView, messageResId, Snackbar.LENGTH_SHORT).show();
                    updateTasks();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = requestFailureMessage(getActivity(), t);
                showErrorAsSnackbarWithRevertTaskDoneStateRetry(message, taskId, actualDoneState);
            }
        });
    }

    private void showErrorAsSnackbarWithRevertTaskDoneStateRetry(String errorMessage, final int taskId, final boolean actualDoneState) {
        Snackbar.make(tasksListView, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        revertTaskDoneState(taskId, actualDoneState);
                    }
                }).show();
    }
}
