package org.hello.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.hello.R;
import org.hello.TaskResult;
import org.hello.TaskResultType;
import org.hello.ViewSwitcherNew;
import org.hello.entity.User;
import org.hello.utils.ConnectionUtils;
import org.hello.utils.JSONUtils;
import org.hello.utils.RestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

public class UserDetailsActivity extends AppCompatActivity {

    private GetUserDetailsTask getUserDetailsTask = null;
    private DeleteUserTask deleteUserTask = null;

    private ViewSwitcherNew viewSwitcher;
    private View baseLayout;
    private String userSelfLink;

    private TextView firstnameTextView;
    private TextView lastnameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        viewSwitcher = new ViewSwitcherNew(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);
        userSelfLink = getIntent().getStringExtra(UsersListActivity.EXTRA_USER_LINK);

        firstnameTextView = (TextView) findViewById(R.id.firstname);
        lastnameTextView = (TextView) findViewById(R.id.lastname);

        Button buttonRetry = (Button) findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptGetUserDetails();
            }
        });

        attemptGetUserDetails();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_delete) {
            attemptDelete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptGetUserDetails() {
        if (getUserDetailsTask != null) {
            return;
        }

        viewSwitcher.showProgressBar();
        getUserDetailsTask = new GetUserDetailsTask();
        getUserDetailsTask.execute();
    }


    private void attemptDelete() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.prompt_delete_user))
                .setPositiveButton(getText(R.string.action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Snackbar.make(baseLayout, R.string.prompt_deletion, Snackbar.LENGTH_SHORT).show();
                        deleteUserTask = new DeleteUserTask();
                        deleteUserTask.execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_details, menu);
        return true;
    }

    private class GetUserDetailsTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(UserDetailsActivity.this)) {
                return TaskResult.noConnection();
            }

            try {
                String url = UserDetailsActivity.this.userSelfLink;
                ResponseEntity<String> responseEntity = RestUtils.getUserDetails(url);
                return TaskResult.ok(responseEntity);
            } catch (ResourceAccessException e) {
                return TaskResult.serverUnavailable();
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            getUserDetailsTask = null;

            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                ResponseEntity<String> responseEntity = (ResponseEntity<String>) taskResult.getResultObject();
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    displayUserDetails(responseEntity);
                } else if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                    displayError(R.string.error_user_already_deleted);
                } else {
                    displayError(R.string.error_unexpected_response);
                    // TODO send report
                }
            } else if (taskResult.getResultType() == TaskResultType.NO_CONNECTION) {
                displayError(R.string.error_no_connection);
            } else if (taskResult.getResultType() == TaskResultType.SERVER_UNAVAILABLE) {
                displayError(R.string.error_server_unavailable);
            }
        }

        @Override
        protected void onCancelled() {
            getUserDetailsTask = null;
            finish();
        }
    }

    private void displayUserDetails(ResponseEntity<String> responseEntity) {
        User user = JSONUtils.parseAsUser(responseEntity.getBody());
        firstnameTextView.setText(user.getFirstname());
        lastnameTextView.setText(user.getLastname());
        viewSwitcher.showMainLayout();
    }

    private void displayError(@StringRes int errorMessageResId) {
        String errorMessage = getString(errorMessageResId);
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }

    private class DeleteUserTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(UserDetailsActivity.this)) {
                return TaskResult.noConnection();
            }

            try {
                String url = UserDetailsActivity.this.userSelfLink;
                ResponseEntity<String> responseEntity = RestUtils.deleteUser(url);
                return TaskResult.ok(responseEntity);
            } catch (ResourceAccessException e) {
                return TaskResult.serverUnavailable();
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            deleteUserTask = null;

            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                ResponseEntity<String> responseEntity = (ResponseEntity<String>) taskResult.getResultObject();
                if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                    setResult(Activity.RESULT_OK, new Intent());
                    finish();
                } else {
                    displayErrorSnackbar(R.string.error_unexpected_response);
                    // TODO send report
                }
            } else if (taskResult.getResultType() == TaskResultType.NO_CONNECTION) {
                displayErrorSnackbar(R.string.error_no_connection);
            } else if (taskResult.getResultType() == TaskResultType.SERVER_UNAVAILABLE) {
                displayErrorSnackbar(R.string.error_server_unavailable);
            }
        }

        @Override
        protected void onCancelled() {
            deleteUserTask = null;
        }
    }

    private void displayErrorSnackbar(@StringRes int errorMessageResId) {
        String errorMessage = getString(errorMessageResId);
        Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptDelete();
                    }
                }).show();
    }
}
