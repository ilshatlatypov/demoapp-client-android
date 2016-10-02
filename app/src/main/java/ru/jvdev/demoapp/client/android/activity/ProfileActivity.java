package ru.jvdev.demoapp.client.android.activity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.utils.CommonUtils;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;

import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;
import static ru.jvdev.demoapp.client.android.utils.SnackbarCustom.LENGTH_SHORT;

public class ProfileActivity extends AppCompatActivity {

    private ListView profileActionsView;
    private AlertDialog changePasswordDialog;
    private ViewSwitcher viewSwitcher;

    private User currentUser;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] profileActions = new String[] { getString(R.string.prompt_change_password) };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, profileActions);

        profileActionsView = (ListView) findViewById(R.id.profile_actions);
        profileActionsView.setAdapter(adapter);
        profileActionsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    showChangePasswordDialog();
                }
            }
        });

        currentUser = ((DemoApp) getApplicationContext()).getActiveUser();
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

    private void showChangePasswordDialog() {
         changePasswordDialog = new AlertDialog.Builder(ProfileActivity.this)
                .setTitle(R.string.title_change_password)
                .setView(R.layout.dialog_new_password)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        changePasswordDialog.show();
        changePasswordDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                attemptChangePassword();
            }
        });
        viewSwitcher = new ViewSwitcher(this, changePasswordDialog, R.id.progress_bar, R.id.new_password_form);
    }

    private void attemptChangePassword() {
        TextInputLayout passwordLayout = ((TextInputLayout) changePasswordDialog.findViewById(R.id.new_password_layout));
        TextInputLayout passwordConfirmLayout = ((TextInputLayout) changePasswordDialog.findViewById(R.id.new_password_confirm_layout));
        passwordLayout.setError(null);
        passwordConfirmLayout.setError(null);

        EditText passwordText = (EditText) changePasswordDialog.findViewById(R.id.new_password);
        EditText passwordConfirmText = (EditText) changePasswordDialog.findViewById(R.id.new_password_confirm);

        CharSequence password = passwordText.getText();
        CharSequence passwordConfirm = passwordConfirmText.getText();

        View errorField = null;
        if (TextUtils.isEmpty(passwordText.getText())) {
            passwordLayout.setError(getString(R.string.error_field_required));
            errorField = passwordText;
        } else if (!TextUtils.equals(password, passwordConfirm)) {
            passwordConfirmLayout.setError(getString(R.string.error_passwords_match));
            errorField = passwordConfirmText;
        }
        // TODO password length, characters

        if (errorField == null) {
            KeyboardUtils.hideKeyboard(this);
            sendChangePasswordRequest(password.toString());
        } else {
            errorField.requestFocus();
        }
    }

    private void sendChangePasswordRequest(final String newPassword) {
        viewSwitcher.showProgressBar();

        Api.Users usersApi = rest(this).getUsersApi();
        usersApi.setPassword(currentUser.getId(), newPassword).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    ((DemoApp) getApplicationContext()).setRestCredentials(currentUser.getUsername(), newPassword);
                    viewSwitcher.showMainLayout();
                    changePasswordDialog.dismiss();
                    Snackbar.make(profileActionsView, R.string.prompt_password_changed, LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = CommonUtils.requestFailureMessage(ProfileActivity.this, t);
                showRequestError(message);
            }
        });
    }

    private void showRequestError(String errorMessage) {
        TextView errorTextView = (TextView) changePasswordDialog.findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        errorTextView.setVisibility(View.VISIBLE);
        viewSwitcher.showMainLayout();
    }
}
