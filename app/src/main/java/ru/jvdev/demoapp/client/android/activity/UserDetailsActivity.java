package ru.jvdev.demoapp.client.android.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UserDto;
import ru.jvdev.demoapp.client.android.utils.HttpCodes;

public class UserDetailsActivity extends AppCompatActivity {

    public static final int RESULT_DELETED = 100;
    public static final int RESULT_NEED_REFRESH = 101;
    public static final String EXTRA_USER = "user";
    private static final int EDIT_REQUEST = 1;

    private DemoApp app = (DemoApp) getApplicationContext();
    private Api.Users usersApi = app.getRestProvider().getUsersApi();

    private ViewSwitcher viewSwitcher;
    private View baseLayout;

    private int userId;
    private User user;
    private TextView fullnameTextView;
    private TextView usernameTextView;
    private TextView passwordTextView;
    private TextView positionTextView;

    private Button retryButton;

    private boolean actionsVisible = false;

    private boolean needParentRefresh = false;

    private boolean deletionInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        baseLayout = findViewById(R.id.base_layout);

        viewSwitcher = new ViewSwitcher(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);
        userId = getIntent().getIntExtra(UsersFragment.EXTRA_USER_ID, 0);

        fullnameTextView = (TextView) findViewById(R.id.fullname);
        usernameTextView = (TextView) findViewById(R.id.username);
        passwordTextView = (TextView) findViewById(R.id.password);
        positionTextView = (TextView) findViewById(R.id.position);


        retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetUserDetailsRequest();
            }
        });

        sendGetUserDetailsRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_user_actions, actionsVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (needParentRefresh) {
                setResult(RESULT_NEED_REFRESH, new Intent());
            }
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            if (!deletionInProgress) {
                openUpdateUserActivity();
            }
        } else if (id == R.id.action_delete) {
            if (!deletionInProgress) {
                attemptDelete();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openUpdateUserActivity() {
        Intent intent = new Intent(this, CreateOrUpdateUserActivity.class);
        intent.putExtra(EXTRA_USER, user);
        startActivityForResult(intent, EDIT_REQUEST);
    }

    private void sendGetUserDetailsRequest() {
        viewSwitcher.showProgressBar();

        Call<UserDto> getUserCall = usersApi.getUser(userId);
        getUserCall.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful()) {
                    user = response.body().toUser();
                    displayUserDetails(user);
                    setActionsOnUserVisible(true);
                } else if (response.code() == HttpCodes.NOT_FOUND) {
                    showUserNotFoundError();
                    setActionsOnUserVisible(false);
                    needParentRefresh = true;
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showErrorOnGettingUserDetails(message);
                setActionsOnUserVisible(false);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(baseLayout, R.string.prompt_user_saved, Snackbar.LENGTH_SHORT).show();
                sendGetUserDetailsRequest();
                needParentRefresh = true;
            }
        }
    }

    private void setActionsOnUserVisible(boolean actionsVisible) {
        this.actionsVisible = actionsVisible;
        invalidateOptionsMenu();
    }

    private void attemptDelete() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.prompt_delete_user))
                .setPositiveButton(getText(R.string.action_delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendDeleteUserRequest(userId);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void sendDeleteUserRequest(int userId) {
        Snackbar.make(baseLayout, R.string.prompt_deletion, Snackbar.LENGTH_INDEFINITE).show();

        deletionInProgress = true;
        Call<Void> deleteUserCall = usersApi.deleteUser(userId);
        deleteUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == HttpCodes.NOT_FOUND) {
                    setResult(RESULT_DELETED, new Intent());
                    finish();
                }
                deletionInProgress = false;
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                showErrorOnUserDelete(message);
                deletionInProgress = false;
            }
        });
    }

    private void displayUserDetails(User user) {
        fullnameTextView.setText(user.getFullname());
        usernameTextView.setText(user.getUsername());
        passwordTextView.setText(user.getPassword());
        positionTextView.setText(user.getRole().toString());
        viewSwitcher.showMainLayout();
    }

    private void showUserNotFoundError() {
        showErrorLayout(getString(R.string.error_user_not_found), View.GONE);
    }

    private void showErrorOnGettingUserDetails(String errorMessage) {
        showErrorLayout(errorMessage, View.VISIBLE);
    }

    private void showErrorLayout(String errorMessage, int retryButtonVisibility) {
        retryButton.setVisibility(retryButtonVisibility);
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }

    private void showErrorOnUserDelete(String errorMessage) {
        Snackbar.make(baseLayout, errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptDelete();
                    }
                }).show();
    }
}
