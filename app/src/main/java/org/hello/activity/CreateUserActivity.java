package org.hello.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.hello.MyService;
import org.hello.R;
import org.hello.entity.User;
import org.hello.entity.dto.UserDto;
import org.hello.utils.KeyboardUtils;
import org.hello.utils.RestUtils;

import java.net.ConnectException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateUserActivity extends AppCompatActivity {

    private MyService service = RestUtils.getService();

    private Button createButton;
    private View baseLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_user);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        createButton = (Button) findViewById(R.id.b_create);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptCreateUser();
            }
        });
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

    private void attemptCreateUser() {
        TextInputLayout firstnameLayout = (TextInputLayout) findViewById(R.id.firstname_layout);
        TextInputLayout lastnameLayout = (TextInputLayout) findViewById(R.id.lastname_layout);
        TextInputLayout usernameLayout = (TextInputLayout) findViewById(R.id.username_layout);
        TextInputLayout passwordLayout = (TextInputLayout) findViewById(R.id.password_layout);
        firstnameLayout.setError(null);
        lastnameLayout.setError(null);
        usernameLayout.setError(null);
        passwordLayout.setError(null);

        EditText firstnameText = (EditText) findViewById(R.id.firstname_text);
        EditText lastnameText = (EditText) findViewById(R.id.lastname_text);
        EditText usernameText = (EditText) findViewById(R.id.username_text);
        EditText passwordText = (EditText) findViewById(R.id.password_text);
        String firstname = firstnameText.getText().toString();
        String lastname = lastnameText.getText().toString();
        String username = usernameText.getText().toString();
        String password = passwordText.getText().toString();

        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_field_required));
            focusView = passwordText;
        }

        if (!containsOnlyLatinLetters(username)) {
            usernameLayout.setError(getString(R.string.error_only_latin_letters));
            focusView = usernameText;
        }
        if (TextUtils.isEmpty(username)) {
            usernameLayout.setError(getString(R.string.error_field_required));
            focusView = usernameText;
        }

        if (TextUtils.isEmpty(lastname)) {
            lastnameLayout.setError(getString(R.string.error_field_required));
            focusView = lastnameText;
        }

        if (TextUtils.isEmpty(firstname)) {
            firstnameLayout.setError(getString(R.string.error_field_required));
            focusView = firstnameText;
        }

        boolean cancel = focusView != null;
        if (cancel) {
            focusView.requestFocus();
        } else {
            KeyboardUtils.hideKeyboard(this);
            User user = new User(firstname, lastname, username, password);
            sendCreateUserRequest(user);
        }
    }

    private void sendCreateUserRequest(User user) {
        Snackbar.make(baseLayout, R.string.prompt_saving, Snackbar.LENGTH_SHORT).show();

        Call<Void> userDtoCall = service.createUser(new UserDto(user));
        userDtoCall.enqueue(new Callback<Void>() {
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
                showError(message);
            }
        });
    }

    private boolean containsOnlyLatinLetters(String s) {
        return s.matches("[A-Za-z]+");
    }

    private void showError(String errorMessage) {
        Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptCreateUser();
                    }
                }).show();
    }
}
