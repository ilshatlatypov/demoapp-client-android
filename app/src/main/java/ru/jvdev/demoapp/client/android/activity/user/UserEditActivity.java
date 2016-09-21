package ru.jvdev.demoapp.client.android.activity.user;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UserDto;
import ru.jvdev.demoapp.client.android.spinner.SpinnerWithChooseItemArrayAdapter;
import ru.jvdev.demoapp.client.android.spinner.SpinnerWithChooseItemListener;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;
import ru.jvdev.demoapp.client.android.utils.StringUtils;

import static ru.jvdev.demoapp.client.android.activity.utils.IntentExtra.OBJECT;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;

public class UserEditActivity extends AppCompatActivity {

    private Api.Users usersApi;
    private int userId;

    private EditText firstnameText;
    private EditText lastnameText;
    private EditText usernameText;
    private EditText passwordText;
    private Spinner positionSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initInputs();

        User user = (User) getIntent().getSerializableExtra(OBJECT);
        if (user != null) {
            userId = user.getId();
            fillFieldsWithData(user);
            getSupportActionBar().setTitle(R.string.title_edit_user);
        }

        usersApi = rest(this).getUsersApi();
    }

    private void initInputs() {
        firstnameText = (EditText) findViewById(R.id.firstname_text);
        lastnameText = (EditText) findViewById(R.id.lastname_text);
        usernameText = (EditText) findViewById(R.id.username_text);
        passwordText = (EditText) findViewById(R.id.password_text);
        passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    attemptCreate();
                    return true;
                }
                return false;
            }
        });

        positionSpinner = (Spinner) findViewById(R.id.position_spinner);
        ArrayAdapter<Role> spinnerAdapter =
                new SpinnerWithChooseItemArrayAdapter<>(this, R.layout.spinner_item, Role.values());
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        positionSpinner.setAdapter(spinnerAdapter);
        positionSpinner.setOnItemSelectedListener(new SpinnerWithChooseItemListener(this));
    }

    private void fillFieldsWithData(User user) {
        firstnameText.setText(user.getFirstname());
        lastnameText.setText(user.getLastname());
        usernameText.setText(user.getUsername());
        passwordText.setText(user.getPassword());
        int pos = ((ArrayAdapter<Role>) positionSpinner.getAdapter()).getPosition(user.getRole());
        positionSpinner.setSelection(pos);
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
                attemptCreate();
            } else {
                attemptUpdate();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    //region Creation
    private void attemptCreate() {
        clearFieldErrorsIfAny();

        User user = init();
        Map<Integer, String> errors = validate(user);

        if (errors.isEmpty()) {
            KeyboardUtils.hideKeyboard(this);
            sendCreateUserRequest(user);
        } else {
            displayFieldErrors(errors);
        }
    }

    private void sendCreateUserRequest(User user) {
        Snackbar.make(content(), R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> call = usersApi.createUser(new UserDto(user));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = requestFailureMessage(UserEditActivity.this, t);
                showErrorAsSnackbarWithRetry(message);
            }
        });
    }
    //endregion

    //region Update
    private void attemptUpdate() {
        clearFieldErrorsIfAny();

        User user = init();
        Map<Integer, String> errors = validate(user);

        if (errors.isEmpty()) {
            KeyboardUtils.hideKeyboard(this);
            sendUpdateUserRequest(userId, user);
        } else {
            displayFieldErrors(errors);
        }
    }

    private void sendUpdateUserRequest(int userId, User user) {
        Snackbar.make(content(), R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> call = usersApi.updateUser(userId, new UserDto(user));
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    setResult(RESULT_OK, new Intent());
                    finish();
                }
                // FIXME: 29.08.16 PUT to non-existing ID creates new instance instead of NOT FOUND
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = requestFailureMessage(UserEditActivity.this, t);
                showErrorAsSnackbarWithRetry(message);
            }
        });
    }
    //endregion

    private void clearFieldErrorsIfAny() {
        LinearLayout fieldsLayout = (LinearLayout) findViewById(R.id.fields);
        for(int i = 0; i < fieldsLayout.getChildCount(); i++ ) {
            if (fieldsLayout.getChildAt(i) instanceof TextInputLayout) {
                ((TextInputLayout) fieldsLayout.getChildAt(i)).setError(null);
            }
        }
    }

    private User init() {
        String firstname = firstnameText.getText().toString();
        String lastname = lastnameText.getText().toString();
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();
        Role role = (Role) positionSpinner.getSelectedItem();
        return new User(firstname, lastname, username, password, role);
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

    private void showErrorAsSnackbarWithRetry(String errorMessage) {
        Snackbar.make(content(), errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptCreate();
                    }
                }).show();
    }

    private View content() {
        return findViewById(android.R.id.content);
    }
}
