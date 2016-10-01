package ru.jvdev.demoapp.client.android.activity;

import android.os.Bundle;
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

import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;

public class ProfileActivity extends AppCompatActivity {

    AlertDialog changePasswordDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] profileActions = new String[] { getString(R.string.prompt_change_password) };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, profileActions);

        ListView profileActionsView = (ListView) findViewById(R.id.profile_actions);
        profileActionsView.setAdapter(adapter);
        profileActionsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if (position == 0) {
                    showChangePasswordDialog();
                }
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

        if (errorField == null) {
            KeyboardUtils.hideKeyboard(this);
            sendChangePasswordRequest(password.toString());
        } else {
            errorField.requestFocus();
        }
    }

    private void sendChangePasswordRequest(String newPassword) {
        changePasswordDialog.dismiss();
    }
}
