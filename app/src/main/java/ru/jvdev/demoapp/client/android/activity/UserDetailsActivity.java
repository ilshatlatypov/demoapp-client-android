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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.UserDto;
import ru.jvdev.demoapp.client.android.utils.ActivityResultCode;
import ru.jvdev.demoapp.client.android.utils.HttpCodes;

import static ru.jvdev.demoapp.client.android.utils.ActivityRequestCode.EDIT;
import static ru.jvdev.demoapp.client.android.utils.ActivityResultCode.NEED_PARENT_REFRESH;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.requestFailureMessage;
import static ru.jvdev.demoapp.client.android.utils.CommonUtils.rest;
import static ru.jvdev.demoapp.client.android.utils.IntentExtra.ID;
import static ru.jvdev.demoapp.client.android.utils.IntentExtra.OBJECT;

public class UserDetailsActivity extends AppCompatActivity {

    private Api.Users usersApi;
    private int userId;
    private User user;

    private ViewSwitcher viewSwitcher;
    private Button retryButton;

    private boolean actionsVisible = false;
    private boolean needParentRefresh = false;
    private boolean deletionInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewSwitcher = new ViewSwitcher(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);
        retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendGetDetailsRequest();
            }
        });

        userId = getIntent().getIntExtra(ID, 0);
        usersApi = rest(this).getUsersApi();

        sendGetDetailsRequest();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (needParentRefresh) {
                setResult(NEED_PARENT_REFRESH, new Intent());
            }
            finish();
            return true;
        } else if (id == R.id.action_edit) {
            if (!deletionInProgress) {
                openEditActivity();
            }
            return true;
        } else if (id == R.id.action_delete) {
            if (!deletionInProgress) {
                attemptDelete();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setActionsOnUserVisible(boolean actionsVisible) {
        this.actionsVisible = actionsVisible;
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.group_user_actions, actionsVisible);
        return super.onPrepareOptionsMenu(menu);
    }

    //region Deletion
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
        Snackbar.make(content(), R.string.prompt_deletion, Snackbar.LENGTH_INDEFINITE).show();

        deletionInProgress = true;
        Call<Void> deleteUserCall = usersApi.deleteUser(userId);
        deleteUserCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful() || response.code() == HttpCodes.NOT_FOUND) {
                    setResult(ActivityResultCode.DELETED, new Intent());
                    finish();
                }
                deletionInProgress = false;
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String message = requestFailureMessage(UserDetailsActivity.this, t);
                showErrorAsSnackbarWithRetry(message);
                deletionInProgress = false;
            }
        });
    }
    //endregion

    //region Details
    private void sendGetDetailsRequest() {
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
                    showErrorLayout(getString(R.string.error_user_not_found), false);
                    setActionsOnUserVisible(false);
                    needParentRefresh = true;
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                String message = requestFailureMessage(UserDetailsActivity.this, t);
                showErrorLayout(message, true);
                setActionsOnUserVisible(false);
            }
        });
    }

    private void displayUserDetails(User user) {
        TextView fullnameView = (TextView) findViewById(R.id.fullname);
        TextView usernameView = (TextView) findViewById(R.id.username);
        TextView passwordView = (TextView) findViewById(R.id.password);
        TextView positionView = (TextView) findViewById(R.id.position);

        fullnameView.setText(user.getFullname());
        usernameView.setText(user.getUsername());
        passwordView.setText(user.getPassword());
        positionView.setText(user.getRole().toString());
        viewSwitcher.showMainLayout();
    }
    //endregion

    private void showErrorLayout(String errorMessage, boolean retryButtonVisible) {
        retryButton.setVisibility(retryButtonVisible ? View.VISIBLE : View.GONE);
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }

    private void showErrorAsSnackbarWithRetry(String errorMessage) {
        Snackbar.make(content(), errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptDelete();
                    }
                }).show();
    }

    //region Related Activities
    private void openEditActivity() {
        Intent intent = new Intent(this, UserEditActivity.class);
        intent.putExtra(OBJECT, user);
        startActivityForResult(intent, EDIT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT) {
            if (resultCode == Activity.RESULT_OK) {
                Snackbar.make(content(), R.string.prompt_user_saved, Snackbar.LENGTH_SHORT).show();
                sendGetDetailsRequest();
                needParentRefresh = true;
            }
        }
    }
    //endregion

    private View content() {
        return findViewById(android.R.id.content);
    }
}
