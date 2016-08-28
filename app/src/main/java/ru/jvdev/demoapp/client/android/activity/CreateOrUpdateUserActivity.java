package ru.jvdev.demoapp.client.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.net.ConnectException;
import java.util.LinkedHashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UserDto;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;
import ru.jvdev.demoapp.client.android.utils.RestProvider;
import ru.jvdev.demoapp.client.android.utils.StringUtils;

public class CreateOrUpdateUserActivity extends AppCompatActivity {

    private Api.Users usersApi = RestProvider.getUsersApi();
    private LinearLayout baseLayout;

    private int userId;
    private EditText firstnameText;
    private EditText lastnameText;
    private EditText usernameText;
    private EditText passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_or_update_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        baseLayout = (LinearLayout) findViewById(R.id.base_layout);

        firstnameText = (EditText) findViewById(R.id.firstname_text);
        lastnameText = (EditText) findViewById(R.id.lastname_text);
        usernameText = (EditText) findViewById(R.id.username_text);
        passwordText = (EditText) findViewById(R.id.password_text);
        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptCreateUser();
                    return true;
                }
                return false;
            }
        });

        User user = (User) getIntent().getSerializableExtra(UserDetailsActivity.EXTRA_USER);
        if (user != null) {
            userId = user.getId();
            fillFieldsWithData(user);
            getSupportActionBar().setTitle(R.string.title_edit_user);
        } else {
            getSupportActionBar().setTitle(R.string.title_new_user);
        }
    }

    private void fillFieldsWithData(User user) {
        firstnameText.setText(user.getFirstname());
        lastnameText.setText(user.getLastname());
        usernameText.setText(user.getUsername());
        passwordText.setText(user.getPassword());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_save) {
            if (userId == 0) {
                attemptCreateUser();
            } else {
                attemptUpdateUser();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void attemptCreateUser() {
        clearFieldErrorsIfAny();

        User user = initUser();
        Map<Integer, String> errors = validate(user);

        if (errors.isEmpty()) {
            KeyboardUtils.hideKeyboard(this);
            sendCreateUserRequest(user);
        } else {
            displayFieldErrors(errors);
        }
    }

    private void sendCreateUserRequest(User user) {
        Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> createUserCall = usersApi.createUser(new UserDto(user));
        createUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = (t instanceof ConnectException) ?
                    getString(R.string.error_server_unavailable) :
                    getString(R.string.error_unknown, t.getMessage());
                showRequestError(message);
            }
        });
    }

    private void attemptUpdateUser() {
        clearFieldErrorsIfAny();

        User user = initUser();
        Map<Integer, String> errors = validate(user);

        if (errors.isEmpty()) {
            KeyboardUtils.hideKeyboard(this);
            sendUpdateUserRequest(userId, user);
        } else {
            displayFieldErrors(errors);
        }
    }

    private void sendUpdateUserRequest(int userId, User user) {
        Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> updateUserCall = usersApi.updateUser(userId, new UserDto(user));
        updateUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
                // TODO user not found
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = (t instanceof ConnectException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showRequestError(message);
            }
        });
    }

    private void clearFieldErrorsIfAny() {
        for(int i = 0; i < baseLayout.getChildCount(); i++ ) {
            if (baseLayout.getChildAt(i) instanceof TextInputLayout) {
                ((TextInputLayout) baseLayout.getChildAt(i)).setError(null);
            }
        }
    }

    private User initUser() {
        String firstname = firstnameText.getText().toString();
        String lastname = lastnameText.getText().toString();
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        return new User(firstname, lastname, username, password);
    }

    private Map<Integer, String> validate(User user) {
        Map<Integer, String> errors = new LinkedHashMap<>();

        if (TextUtils.isEmpty(user.getFirstname())) {
            errors.put(R.id.firstname_layout, getString(R.string.error_field_required));
        }

        if (TextUtils.isEmpty(user.getLastname())) {
            errors.put(R.id.lastname_layout, getString(R.string.error_field_required));
        }

        if (!StringUtils.containsOnlyLatinLetters(user.getUsername())) {
            errors.put(R.id.username_layout, getString(R.string.error_only_latin_letters));
        }
        if (TextUtils.isEmpty(user.getUsername())) {
            errors.put(R.id.username_layout, getString(R.string.error_field_required));
        }

        if (TextUtils.isEmpty(user.getPassword())) {
            errors.put(R.id.password_layout, getString(R.string.error_field_required));
        }
        return errors;
    }

    private void displayFieldErrors(Map<Integer, String> errors) {
        boolean focusSet = false;
        for (Integer textLayoutId : errors.keySet()) {
            String errorMessage = errors.get(textLayoutId);
            TextInputLayout layout = ((TextInputLayout) findViewById(textLayoutId));
            layout.setError(errorMessage);
            if (!focusSet) { // set focus to first EditText with error
                layout.getChildAt(0).requestFocus();
                focusSet = true;
            }
        }
    }

    private void showRequestError(String errorMessage) {
        Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptCreateUser();
                    }
                }).show();
    }
}
