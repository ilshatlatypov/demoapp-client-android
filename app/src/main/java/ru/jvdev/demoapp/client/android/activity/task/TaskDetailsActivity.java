package ru.jvdev.demoapp.client.android.activity.task;

import android.app.Activity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.dto.TaskDto;
import ru.jvdev.demoapp.client.android.utils.DateUtils;
import ru.jvdev.demoapp.client.android.utils.HttpCodes;

import static ru.jvdev.demoapp.client.android.activity.utils.ActivityRequestCode.EDIT;
import static ru.jvdev.demoapp.client.android.activity.utils.ActivityResultCode.NEED_PARENT_REFRESH;
import static ru.jvdev.demoapp.client.android.activity.utils.IntentExtra.ID;
import static ru.jvdev.demoapp.client.android.activity.utils.IntentExtra.OBJECT;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;

public class TaskDetailsActivity extends AppCompatActivity {

    private Api.Tasks tasksApi;
    private int taskId;
    private Task task;

    private ViewSwitcher viewSwitcher;
    private Button retryButton;

    private boolean actionsVisible = false;
    private boolean needParentRefresh = false;
    private boolean updateInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewSwitcher = new ViewSwitcher(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);
        retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetDetailsRequest();
            }
        });

        taskId = getIntent().getIntExtra(ID, 0);
        tasksApi = rest(this).getTasksApi();

        sendGetDetailsRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (needParentRefresh) {
                setResult(NEED_PARENT_REFRESH, new Intent());
            }
            finish();
        } else if (id == R.id.action_edit) {
            if (!updateInProgress) {
                openEditActivity();
            }
        } else if (id == R.id.action_delete) {
            if (!updateInProgress) {
                attemptDelete();
            }
        } else if (id == R.id.action_done) {
            markTaskAsDone();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setActionsOnTaskVisible(boolean actionsVisible) {
        this.actionsVisible = actionsVisible;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_task_actions, actionsVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    private void markTaskAsDone() {
        Snackbar.make(content(), R.string.prompt_sending_request, Snackbar.LENGTH_INDEFINITE).show();

        updateInProgress = true;
        Task task = new Task();
        task.setDone(true);
        Call<Void> deleteUserCall = tasksApi.patch(taskId, new TaskDto(task));
        deleteUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent();
                    intent.putExtra(ID, taskId);
                    setResult(ActivityResultCode.MARKED_AS_DONE, intent);
                    finish();
                }
                updateInProgress = false;
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = requestFailureMessage(TaskDetailsActivity.this, t);
                showErrorAsSnackbarWithMarkAsDoneRetry(message);
                updateInProgress = false;
            }
        });
    }

    //region Deletion
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
        Snackbar.make(content(), R.string.prompt_deletion, Snackbar.LENGTH_INDEFINITE).show();

        updateInProgress = true;
        Call<Void> deleteCall = tasksApi.delete(taskId);
        deleteCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == HttpCodes.NOT_FOUND) {
                    setResult(ActivityResultCode.DELETED, new Intent());
                    finish();
                }
                updateInProgress = false;
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = requestFailureMessage(TaskDetailsActivity.this, t);
                showErrorAsSnackbarWithDeleteRetry(message);
                updateInProgress = false;
            }
        });
    }
    //endregion

    //region Details
    private void sendGetDetailsRequest() {
        viewSwitcher.showProgressBar();

        Call<TaskDto> call = tasksApi.get(taskId);
        call.enqueue(new Callback<TaskDto>() {
            @Override
            public void onResponse(Call<TaskDto> call, Response<TaskDto> response) {
                if (response.isSuccessful()) {
                    task = response.body().toTask();
                    displayTaskDetails(task);
                    setActionsOnTaskVisible(true);
                } else if (response.code() == HttpCodes.NOT_FOUND) {
                    showErrorLayout(getString(R.string.error_task_not_found), false);
                    setActionsOnTaskVisible(false);
                    needParentRefresh = true;
                }
            }

            @Override
            public void onFailure(Call<TaskDto> call, Throwable t) {
                String message = requestFailureMessage(TaskDetailsActivity.this, t);
                showErrorLayout(message, true);
                setActionsOnTaskVisible(false);
            }
        });
    }

    private void displayTaskDetails(Task task) {
        ((TextView) findViewById(R.id.title)).setText(task.getTitle());
        ((TextView) findViewById(R.id.date)).setText(DateUtils.dateToString(this, task.getDate()));
        ((TextView) findViewById(R.id.user)).setText(task.getUser() != null ? task.getUser().toString() : "не назначен");
        viewSwitcher.showMainLayout();
    }
    //endregion

    private void showErrorLayout(String errorMessage, boolean retryButtonVisible) {
        retryButton.setVisibility(retryButtonVisible ? View.VISIBLE : View.GONE);
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }

    private void showErrorAsSnackbarWithDeleteRetry(String errorMessage) {
        Snackbar.make(content(), errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptDelete();
                    }
                }).show();
    }

    private void showErrorAsSnackbarWithMarkAsDoneRetry(String errorMessage) {
        Snackbar.make(content(), errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        markTaskAsDone();
                    }
                }).show();
    }

    //region Related Activities
    private void openEditActivity() {
        Intent intent = new Intent(this, TaskEditActivity.class);
        intent.putExtra(OBJECT, task);
        startActivityForResult(intent, EDIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(content(), R.string.prompt_task_saved, Snackbar.LENGTH_SHORT).show();
                sendGetDetailsRequest();
                needParentRefresh = true;
            }
        }
    }
    //endregion

    private View content() {
        return findViewById(android.R.id.content);
    }
}
