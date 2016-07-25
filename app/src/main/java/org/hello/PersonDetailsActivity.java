package org.hello;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class PersonDetailsActivity extends AppCompatActivity {

    public static final int RESULT_PERSON_DELETED = 1;

    private View baseLayout;
    private Person person;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        person = (Person) getIntent().getSerializableExtra(PersonsListActivity.EXTRA_PERSON);
        ((TextView) findViewById(R.id.firstname)).setText(person.getFirstName());
        ((TextView) findViewById(R.id.lastname)).setText(person.getLastName());
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

    private class DeletePersonTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!isConnected(PersonDetailsActivity.this)) {
                return new TaskResult(TaskResultType.NO_CONNECTION);
            }

            try {
                String url = PersonDetailsActivity.this.person.getSelfLink();
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        url, HttpMethod.DELETE, null,
                        String.class);
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
            if (taskResult.resultType == TaskResultType.SUCCESS) {
                setResult(RESULT_PERSON_DELETED, new Intent());
                finish();
            } else {
                String errorMessage = null;
                switch (taskResult.resultType) {
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

    public static boolean isConnected(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
