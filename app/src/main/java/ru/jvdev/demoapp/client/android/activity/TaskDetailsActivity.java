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

import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Task;

import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.EDIT;
import static ru.jvdev.demoapp.client.android.utils.IntentExtra.ID;

public class TaskDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TASK = "task";

    private View baseLayout;

    private int taskId;
    private Task task;

    private boolean deletionInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.activity_task_details);

        taskId = getIntent().getIntExtra(ID, 0);
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
        Snackbar.make(baseLayout, "Sending delete request...", Snackbar.LENGTH_SHORT).show();
    }
}
