package ru.jvdev.demoapp.client.android.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.dto.TaskDto;
import ru.jvdev.demoapp.client.android.utils.ActivityResult;
import ru.jvdev.demoapp.client.android.utils.HttpCodes;

import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.EDIT;
import static ru.jvdev.demoapp.client.android.utils.IntentExtra.ID;

public class TaskDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "task";

    private Api.Tasks tasksApi;

    private ViewSwitcher viewSwitcher;
    private View baseLayout;

    private int taskId;
    private Task task;

    private TextView titleView;
    private TextView dateView;
    private TextView userView;

    private Button retryButton;

    private boolean actionsVisible = false;
    private boolean needParentRefresh = false;
    private boolean deletionInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.activity_task_details);

        viewSwitcher = new ViewSwitcher(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);
        taskId = getIntent().getIntExtra(ID, 0);

        titleView = (TextView) findViewById(R.id.title);
        dateView = (TextView) findViewById(R.id.date);
        userView = (TextView) findViewById(R.id.user);

        retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetTaskDetailsRequest();
            }
        });

        DemoApp app = (DemoApp) getApplicationContext();
        tasksApi = app.getRestProvider().getTasksApi();

        sendGetTaskDetailsRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_user_actions, actionsVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (needParentRefresh) {
                setResult(ActivityResult.NEED_PARENT_REFRESH, new Intent());
            }
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            if (!deletionInProgress) {
                openUpdateTaskActivity();
            }
        } else if (id == R.id.action_delete) {
            if (!deletionInProgress) {
                attemptDelete();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openUpdateTaskActivity() {
        Intent intent = new Intent(this, CreateOrUpdateTaskActivity.class);
        intent.putExtra(EXTRA_TASK, task);
        startActivityForResult(intent, EDIT);
    }

    private void attemptDelete() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.prompt_delete_task))
                .setPositiveButton(getText(R.string.action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDeleteTaskRequest(taskId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void sendDeleteTaskRequest(int taskId) {
        Snackbar.make(baseLayout, R.string.prompt_deletion, Snackbar.LENGTH_INDEFINITE).show();

        deletionInProgress = true;
        Call<Void> deleteUserCall = tasksApi.delete(taskId);
        deleteUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == HttpCodes.NOT_FOUND) {
                    setResult(ActivityResult.DELETED, new Intent());
                    finish();
                }
                deletionInProgress = false;
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showErrorOnTaskDelete(message);
                deletionInProgress = false;
            }
        });
    }

    private void sendGetTaskDetailsRequest() {
        viewSwitcher.showProgressBar();

        Call<TaskDto> getTaskCall = tasksApi.get(taskId);
        getTaskCall.enqueue(new Callback<TaskDto>() {
            @Override
            public void onResponse(Call<TaskDto> call, Response<TaskDto> response) {
                if (response.isSuccessful()) {
                    task = response.body().toTask();
                    displayTaskDetails(task);
                    setActionsOnTaskVisible(true);
                } else if (response.code() == HttpCodes.NOT_FOUND) {
                    showTaskNotFoundError();
                    setActionsOnTaskVisible(false);
                    needParentRefresh = true;
                }
            }

            @Override
            public void onFailure(Call<TaskDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showErrorOnGettingTaskDetails(message);
                setActionsOnTaskVisible(false);
            }
        });
    }

    private void setActionsOnTaskVisible(boolean actionsVisible) {
        this.actionsVisible = actionsVisible;
        invalidateOptionsMenu();
    }

    private void displayTaskDetails(Task task) {
        titleView.setText(task.getTitle());
        dateView.setText(task.getDate().toString());
        userView.setText(task.getUser() != null ? task.getUser().toString() : "не назначен");
        viewSwitcher.showMainLayout();
    }

    private void showTaskNotFoundError() {
        showErrorLayout(getString(R.string.error_user_not_found), View.GONE);
    }

    private void showErrorOnGettingTaskDetails(String errorMessage) {
        showErrorLayout(errorMessage, View.VISIBLE);
    }

    private void showErrorLayout(String errorMessage, int retryButtonVisibility) {
        retryButton.setVisibility(retryButtonVisibility);
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }

    private void showErrorOnTaskDelete(String errorMessage) {
        Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptDelete();
                    }
                }).show();
    }
}
