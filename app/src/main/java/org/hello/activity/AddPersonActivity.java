package org.hello.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.hello.utils.ConnectionUtils;
import org.hello.entity.Person;
import org.hello.R;
import org.hello.TaskResult;
import org.hello.TaskResultType;
import org.hello.utils.JSONUtils;
import org.hello.utils.KeyboardUtils;
import org.hello.utils.RestUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AddPersonActivity extends AppCompatActivity {

    private Button bCreate;
    private View baseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_person);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        bCreate = (Button) findViewById(R.id.b_create);
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreate();
            }
        });
    }

    private void attemptCreate() {
        TextInputLayout tilFirstName = (TextInputLayout) findViewById(R.id.til_firstname);
        TextInputLayout tilLastName = (TextInputLayout) findViewById(R.id.til_lastname);
        tilFirstName.setError(null);
        tilLastName.setError(null);

        EditText etFirstName = (EditText) findViewById(R.id.et_firstname);
        EditText etLastName = (EditText) findViewById(R.id.et_lastname);
        String firstName = etFirstName.getText().toString();
        String lastName = etLastName.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(lastName)) {
            tilLastName.setError(getString(R.string.error_field_required));
            focusView = etLastName;
            cancel = true;
        }
        if (TextUtils.isEmpty(firstName)) {
            tilFirstName.setError(getString(R.string.error_field_required));
            focusView = etFirstName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            KeyboardUtils.hideKeyboard(this);
            Snackbar.make(baseLayout, R.string.prompt_adding_person, Snackbar.LENGTH_SHORT).show();
            new CreatePersonTask(new Person(firstName, lastName)).execute();
        }
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

    private class CreatePersonTask extends AsyncTask<Void, Void, TaskResult> {

        private final Person person;

        CreatePersonTask(Person person) {
            this.person = person;
        }

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(AddPersonActivity.this)) {
                return new TaskResult(TaskResultType.NO_CONNECTION);
            }

            try {
                String personJson = JSONUtils.toJSON(person).toString();
                ResponseEntity<String> responseEntity = RestUtils.createPerson(personJson);
                HttpStatus httpStatus = responseEntity.getStatusCode();
                if (httpStatus == HttpStatus.CREATED) {
                    return new TaskResult(TaskResultType.SUCCESS);
                } else {
                    return new TaskResult(TaskResultType.UNEXPECTED_RESPONSE_CODE);
                }
            } catch (Exception e) {
                return new TaskResult(TaskResultType.SERVER_UNAVAILABLE);
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                setResult(Activity.RESULT_OK, new Intent());
                finish();
                return;
            }

            String errorMessage = null;
            switch (taskResult.getResultType()) {
                case UNEXPECTED_RESPONSE_CODE:
                    errorMessage = getString(R.string.error_unexpected_response);
                    break;
                case SERVER_UNAVAILABLE:
                    errorMessage = getString(R.string.error_server_unavailable);
                    break;
                case NO_CONNECTION:
                    errorMessage = getString(R.string.error_no_connection);
                    break;
            }
            Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_retry, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            attemptCreate();
                        }
                    }).show();
        }
    }
}