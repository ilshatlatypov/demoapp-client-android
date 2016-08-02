package org.hello.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.hello.R;
import org.hello.TaskResult;
import org.hello.TaskResultType;
import org.hello.ViewSwitcherNew;
import org.hello.entity.User;
import org.hello.utils.ConnectionUtils;
import org.hello.utils.JSONUtils;
import org.hello.utils.RestUtils;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

public class UsersListActivity extends AppCompatActivity {

    private static final int ADD_USER_REQUEST = 1;
    private static final int USER_DETAILS_REQUEST = 2;
    public static final String EXTRA_USER_LINK = "user_link";

    private UpdateUsersTask updateUsersTask = null;

    private ViewSwitcherNew viewSwitcher;
    private ListView lvPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddUserActivity();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewSwitcher = new ViewSwitcherNew(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);

        ArrayAdapter<User> peopleListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvPeople = (ListView) findViewById(R.id.lv_people);
        lvPeople.setAdapter(peopleListAdapter);
        lvPeople.setEmptyView(findViewById(android.R.id.empty));

        lvPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = (User) parent.getItemAtPosition(position);
                openUserDetailsActivity(user);
            }
        });

        Button buttonRetry = (Button) findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUsers();
            }
        });

        Button buttonAddUser = (Button) findViewById(R.id.button_add_user_from_empty_list);
        buttonAddUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddUserActivity();
            }
        });

        updateUsers();
    }

    private void openAddUserActivity() {
        Intent intent = new Intent(this, AddUserActivity.class);
        this.startActivityForResult(intent, ADD_USER_REQUEST);
    }

    private void openUserDetailsActivity(User user) {
        Intent intent = new Intent(this, UserDetailsActivity.class);
        intent.putExtra(EXTRA_USER_LINK, user.getSelfLink());
        this.startActivityForResult(intent, USER_DETAILS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_USER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(lvPeople, R.string.prompt_user_added, Snackbar.LENGTH_SHORT).show();
                updateUsers();
            }
        } else if (requestCode == USER_DETAILS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(lvPeople, R.string.prompt_user_deleted, Snackbar.LENGTH_SHORT).show();
                updateUsers();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUsers() {
        if (updateUsersTask != null) {
            return;
        }

        viewSwitcher.showProgressBar();
        updateUsersTask = new UpdateUsersTask();
        updateUsersTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.users_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateUsers();
        }
        return super.onOptionsItemSelected(item);
    }

    private class UpdateUsersTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(UsersListActivity.this)) {
                return TaskResult.noConnection();
            }

            try {
                return TaskResult.ok(RestUtils.getUsersList());
            } catch (ResourceAccessException e) {
                return TaskResult.serverUnavailable();
            }
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {
            updateUsersTask = null;

            if (taskResult.getResultType() == TaskResultType.SUCCESS) {
                ResponseEntity<String> responseEntity = (ResponseEntity<String>) taskResult.getResultObject();
                if (responseEntity.getStatusCode() == HttpStatus.OK) {
                    putDataToList(responseEntity);
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
            updateUsersTask = null;
            viewSwitcher.showMainLayout();
        }
    }

    private void putDataToList(ResponseEntity<String> responseEntity) {
        List<User> users = null;
        try {
            users = JSONUtils.parseAsUsersList(responseEntity.getBody());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<User> usersListAdapter = (ArrayAdapter<User>) lvPeople.getAdapter();
        usersListAdapter.clear();
        usersListAdapter.addAll(users);
        usersListAdapter.notifyDataSetChanged();
        viewSwitcher.showMainLayout();
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
        viewSwitcher.showErrorLayout();
    }
}
