package org.hello;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class CreatePersonActivity extends AppCompatActivity {

    private ViewSwitcher viewSwitcher;
    private Button bCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_person);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        bCreate = (Button) findViewById(R.id.b_create);
        bCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreate();
            }
        });

        viewSwitcher = new ViewSwitcher(this, R.id.pb_create_person, R.id.create_person_form);
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
            hideKeyboard();
            viewSwitcher.showFirst();
            Person person = new Person(firstName, lastName);
            new CreatePersonTask(person).execute();
        }
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    private class CreatePersonTask extends AsyncTask<Void, Void, Boolean> {

        private final Person person;

        CreatePersonTask(Person person) {
            this.person = person;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                final String url = "http://192.168.2.11:8080/people"; // TODO extract URL and RestTemplate instantiation
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());

                String jsonStr = toJSON(person).toString();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);

                HttpEntity<String> entity = new HttpEntity<>(jsonStr, headers);

                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
                return responseEntity.getStatusCode() == HttpStatus.CREATED;
            } catch (Exception e) {
                Log.e("CreatePersonActivity", e.getMessage(), e);
            }
            return null;
        }

        private JSONObject toJSON(Person person) {
            JSONObject json = new JSONObject();
            try {
                json.put("firstName", person.getFirstName());
                json.put("lastName", person.getLastName());
            } catch (JSONException e) {
                // TODO handle this
            }
            return json;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            viewSwitcher.showSecond();

            if (success == null) {
                // TODO show error message in the bottom with retry button
            }

            if (success) {
                Toast.makeText(CreatePersonActivity.this, R.string.toast_person_created, Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else {
                // TODO what if success == false
            }
        }

        @Override
        protected void onCancelled() {
            viewSwitcher.showSecond();
        }
    }
}