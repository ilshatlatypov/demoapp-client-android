package org.hello.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.hello.R;
import org.hello.TaskResult;
import org.hello.TaskResultType;
import org.hello.entity.User;
import org.hello.utils.ConnectionUtils;
import org.hello.utils.JSONUtils;
import org.hello.utils.KeyboardUtils;
import org.hello.utils.RestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CreateUserActivity extends AppCompatActivity {

    private static final int MIN_LENGTH = 4;
    private static final int MAX_LENGTH = 20;

    private AddUserTask addUserTask = null;

    private Button bCreate;
    private View baseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        bCreate = (Button) findViewById(R.id.b_create);
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptAddUser();
            }
        });
    }

    private void attemptAddUser() {
        if (addUserTask != null) {
            return;
        }

        TextInputLayout tilFirstName = (TextInputLayout) findViewById(R.id.til_firstname);
        TextInputLayout tilLastName = (TextInputLayout) findViewById(R.id.til_lastname);
        TextInputLayout tilLogin = (TextInputLayout) findViewById(R.id.til_login);
        TextInputLayout tilPassword = (TextInputLayout) findViewById(R.id.til_password);
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilLogin.setError(null);
        tilPassword.setError(null);

        EditText etFirstName = (EditText) findViewById(R.id.et_firstname);
        EditText etLastName = (EditText) findViewById(R.id.et_lastname);
        EditText etLogin = (EditText) findViewById(R.id.et_login);
        EditText etPassword = (EditText) findViewById(R.id.et_password);
        String firstname = etFirstName.getText().toString();
        String lastname = etLastName.getText().toString();
        String login = etLogin.getText().toString();
        String password = etPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (!isValidLength(password)) {
            tilPassword.setError(getString(R.string.error_invalid_length, MIN_LENGTH, MAX_LENGTH));
            focusView = etPassword;
            cancel = true;
        }
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_field_required));
            focusView = etPassword;
            cancel = true;
        }

        if (!isValidLength(login)) {
            tilLogin.setError(getString(R.string.error_invalid_length, MIN_LENGTH, MAX_LENGTH));
            focusView = etLogin;
            cancel = true;
        }
        if (!containsOnlyLatinLetters(login)) {
            tilLogin.setError(getString(R.string.error_only_latin_letters));
            focusView = etLogin;
            cancel = true;
        }
        if (TextUtils.isEmpty(login)) {
            tilLogin.setError(getString(R.string.error_field_required));
            focusView = etLogin;
            cancel = true;
        }

        if (!isValidLength(lastname)) {
            tilLastName.setError(getString(R.string.error_invalid_length, MIN_LENGTH, MAX_LENGTH));
            focusView = etLastName;
            cancel = true;
        }
        if (TextUtils.isEmpty(lastname)) {
            tilLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
            cancel = true;
        }

        if (!isValidLength(firstname)) {
            tilFirstName.setError(getString(R.string.error_invalid_length, MIN_LENGTH, MAX_LENGTH));
            focusView = etFirstName;
            cancel = true;
        }
        if (TextUtils.isEmpty(firstname)) {
            tilFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            KeyboardUtils.hideKeyboard(this);
            Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();
            User user = new User(firstname, lastname, login, password);
            addUserTask = new AddUserTask(user);
            addUserTask.execute();
        }
    }

    private boolean containsOnlyLatinLetters(String login) {
        return login.matches("[A-Za-z]+");
    }

    private boolean isValidLength(String value) {
        int length = value.length();
        return MIN_LENGTH <= length && length <= MAX_LENGTH;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class AddUserTask extends AsyncTask<Void, Void, TaskResult> {

        private final User user;

        AddUserTask(User user) {
            this.user = user;
        }

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(CreateUserActivity.this)) {
                return TaskResult.noConnection();
            }

            try {
                String userJson = JSONUtils.toJSON(user).toString();
                ResponseEntity<String> responseEntity = RestUtils.createUser(userJson);
                return TaskResult.ok(responseEntity);
            } catch (Exception e) {
                return TaskResult.serverUnavailable();
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            addUserTask = null;

            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                ResponseEntity<String> responseEntity = (ResponseEntity<String>) taskResult.getResultObject();
                HttpStatus httpStatus = responseEntity.getStatusCode();
                if (httpStatus == HttpStatus.CREATED) {
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

        private void displayErrorSnackbar(@StringRes int errorMessageResId) {
            String errorMessage = getString(errorMessageResId);
            Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            attemptAddUser();
                        }
                    }).show();
        }
    }
}
