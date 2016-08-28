package ru.jvdev.demoapp.client.android.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.TaskResult;
import ru.jvdev.demoapp.client.android.TaskResultType;
import ru.jvdev.demoapp.client.android.utils.ConnectionUtils;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;

/**
 * A login screen that offers login via login/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mLoginView = (EditText) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.username || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        ((TextInputLayout) mLoginView.getParent()).setError(null);
        ((TextInputLayout) mPasswordView.getParent()).setError(null);
        findViewById(R.id.error_text).setVisibility(View.GONE);

        // Store values at the time of the login attempt.
        String login = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(password)) {
            ((TextInputLayout) mPasswordView.getParent()).setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid login.
        if (TextUtils.isEmpty(login)) {
            ((TextInputLayout) mLoginView.getParent()).setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            KeyboardUtils.hideKeyboard(this);
            showProgress(true);
            mAuthTask = new UserLoginTask(login, password);
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, TaskResult> {

        private final String mLogin;
        private final String mPassword;

        UserLoginTask(String login, String password) {
            mLogin = login;
            mPassword = password;
        }

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(LoginActivity.this)) {
                return TaskResult.noConnection();
            }

            try {
                String url = "http://192.168.2.11:8080";
//                DigestAuthRestTemplate restTemplate = DigestAuthRestTemplate.getInstance();
//                restTemplate.setCredentials(mLogin, mPassword);
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
                return TaskResult.ok(responseEntity);
            } catch (ResourceAccessException e) {
                return TaskResult.serverUnavailable();
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            mAuthTask = null;

            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                ResponseEntity<String> responseEntity = (ResponseEntity<String>) taskResult.getResultObject();
                HttpStatus statusCode = responseEntity.getStatusCode();
                if (statusCode == HttpStatus.OK) {
                    gotoMainActivity();
                } else if (statusCode == HttpStatus.UNAUTHORIZED) {
                    displayIncorrectLoginPasswordError();
                } else {
                    displayUnexpectedResponseError();
                    // TODO send report
                }
            } else if (taskResult.getResultType() == TaskResultType.NO_CONNECTION) {
                displayNoConnectionError();
            } else if (taskResult.getResultType() == TaskResultType.SERVER_UNAVAILABLE) {
                displayServerUnavailableError();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void displayIncorrectLoginPasswordError() {
        displayError(R.string.error_incorrect_login_password);
    }

    private void displayServerUnavailableError() {
        displayError(R.string.error_server_unavailable);
    }

    private void displayNoConnectionError() {
        displayError(R.string.error_no_connection);
    }

    private void displayUnexpectedResponseError() {
        displayError(R.string.error_unexpected_response);
    }

    private void displayError(@StringRes int errorMessageResId) {
        String errorMessage = getString(errorMessageResId);
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
        showProgress(false);
    }
}

