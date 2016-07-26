package org.hello;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class PersonDetailsActivity extends AppCompatActivity {

    private ViewSwitcherNew viewSwitcher;
    private View baseLayout;
    private String personSelfLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        viewSwitcher = new ViewSwitcherNew(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);
        personSelfLink = getIntent().getStringExtra(PersonsListActivity.EXTRA_PERSON_LINK);

        Button buttonRetry = (Button) findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptGetPersonDetails();
            }
        });

        attemptGetPersonDetails();
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

    private void attemptGetPersonDetails() {
        viewSwitcher.showProgressBar();
        new GetPersonDetailsTask().execute();
    }


    private void attemptDelete() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.prompt_delete_person))
                .setPositiveButton(getText(R.string.action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Snackbar.make(baseLayout, R.string.prompt_deletion, Snackbar.LENGTH_SHORT).show();
                        new DeletePersonTask().execute();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.person_details, menu);
        return true;
    }

    private class GetPersonDetailsTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(PersonDetailsActivity.this)) {
                return new TaskResult(TaskResultType.NO_CONNECTION);
            }

            try {
                String personUrl = PersonDetailsActivity.this.personSelfLink;
                ResponseEntity<String> responseEntity = RestUtils.getPersonDetails(personUrl);
                HttpStatus httpStatus = responseEntity.getStatusCode();
                if (httpStatus == HttpStatus.OK) { // TODO not found
                    Person person = JSONUtils.parseAsPerson(responseEntity.getBody());
                    return new TaskResult(person);
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
                Person person = (Person) taskResult.getResultObject();
                ((TextView) findViewById(R.id.firstname)).setText(person.getFirstName());
                ((TextView) findViewById(R.id.lastname)).setText(person.getLastName());
                viewSwitcher.showMainLayout();
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
            TextView errorTextView = (TextView) findViewById(R.id.error_text);
            errorTextView.setText(errorMessage);
            viewSwitcher.showErrorLayout();
        }
    }

    private class DeletePersonTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(PersonDetailsActivity.this)) {
                return new TaskResult(TaskResultType.NO_CONNECTION);
            }

            try {
                String personUrl = PersonDetailsActivity.this.personSelfLink;
                ResponseEntity<String> responseEntity = RestUtils.deletePerson(personUrl);
                HttpStatus httpStatus = responseEntity.getStatusCode();
                if (httpStatus == HttpStatus.NO_CONTENT) {
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
                            attemptDelete();
                        }
                    }).show();
        }
    }
}
