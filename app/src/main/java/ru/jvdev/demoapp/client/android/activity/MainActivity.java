package ru.jvdev.demoapp.client.android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.jvdev.demoapp.client.android.Api;
import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.entity.Task;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.entity.dto.TasksPageDto;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentDataLoadingListener {

    private ViewSwitcher viewSwitcher;
    private Fragment activeFragment;

    private ListView tasksListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        User activeUser = ((DemoApp) getApplicationContext()).getActiveUser();
        ((TextView) header.findViewById(R.id.username)).setText(activeUser.getFullname());

        viewSwitcher = new ViewSwitcher(this, R.id.progress_bar, R.id.content_frame, R.id.error_layout);
        Button retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActiveFragment();
            }
        });

        tasksListView = (ListView) findViewById(R.id.tasks_list_view);
        TasksWithSubheadersAdapter adapter = new TasksWithSubheadersAdapter(this);
        tasksListView.setAdapter(adapter);

        updateTasks();
    }

    private void updateTasks() {
        DemoApp app = (DemoApp) getApplicationContext();
        Api.Tasks tasksApi = app.getRestProvider().getTasksApi();

        Call<TasksPageDto> tasksPageDtoCall = tasksApi.getTasks();
        tasksPageDtoCall.enqueue(new Callback<TasksPageDto>() {
            @Override
            public void onResponse(Call<TasksPageDto> call, Response<TasksPageDto> response) {
                List<Task> tasks = response.body().getTasks();
                tasks.add(0, new Task(0, "Сегодня"));
                putDataToList(tasks);
                MainActivity.this.onDataLoaded();
            }

            @Override
            public void onFailure(Call<TasksPageDto> call, Throwable t) {
                String message = (t instanceof ConnectException || t instanceof SocketTimeoutException) ?
                        getString(R.string.error_server_unavailable) :
                        getString(R.string.error_unknown, t.getMessage());
                MainActivity.this.onError(message);
            }
        });
    }

    private void putDataToList(List<Task> tasks) {
        TasksWithSubheadersAdapter adapter = (TasksWithSubheadersAdapter) tasksListView.getAdapter();
        adapter.setTasks(tasks);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshActiveFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_users || id == R.id.nav_tasks) {
            displayFragmentByNavItemId(id);
        } else if (id == R.id.nav_logout) {
            ((DemoApp) getApplicationContext()).setActiveUser(null);
            gotoLoginActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void setActiveFragment(android.app.Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
        activeFragment = fragment;
    }

    private void displayFragmentByNavItemId(@IdRes int itemId) {
        viewSwitcher.showProgressBar();
        if (itemId == R.id.nav_users) {
            getSupportActionBar().setTitle(R.string.title_users);
            setActiveFragment(UsersFragment.newInstance());
        } else if (itemId == R.id.nav_tasks) {
            getSupportActionBar().setTitle(R.string.title_tasks);
            setActiveFragment(TasksFragment.newInstance());
        }
    }

    private void refreshActiveFragment() {
        if (activeFragment != null && activeFragment instanceof RefreshableFragment) {
            viewSwitcher.showProgressBar();
            ((RefreshableFragment) activeFragment).refreshFragmentData();
        }
    }

    @Override
    public void onDataLoaded() {
        viewSwitcher.showMainLayout();
    }

    @Override
    public void onError(String errorMessage) {
        TextView errorTextView = (TextView) findViewById(R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }
}
