package ru.jvdev.demoapp.client.android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.activity.task.TasksFragment;
import ru.jvdev.demoapp.client.android.activity.user.UsersFragment;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String STATE_ACTIVE_NAV_ITEM = "activeNavItemId";

    private Fragment activeFragment;
    private int activeNavItemId;

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

        if (activeUser.getRole() == Role.EMPLOYEE) {
            navigationView.getMenu().findItem(R.id.nav_users).setVisible(false);
        }

        if (savedInstanceState == null) {
            activeNavItemId = R.id.nav_tasks;
        } else {
            activeNavItemId = savedInstanceState.getInt(STATE_ACTIVE_NAV_ITEM);
        }

        if (activeNavItemId == R.id.nav_users) {
            showUsersFragment();
        } else if (activeNavItemId == R.id.nav_tasks) {
            showTasksFragment();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ACTIVE_NAV_ITEM, activeNavItemId);
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

        if (id == R.id.nav_users) {
            showUsersFragment();
        } else if (id == R.id.nav_tasks) {
            showTasksFragment();
        } else if (id == R.id.nav_logout) {
            ((DemoApp) getApplicationContext()).setActiveUser(null);
            gotoLoginActivity();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showTasksFragment() {
        ActionBar ab = getSupportActionBar();

        View view = LayoutInflater.from(ab.getThemedContext()).inflate(R.layout.toolbar_with_spinner, null);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);

        ab.setTitle("");
        ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowCustomEnabled(true);
        ab.setCustomView(toolbar);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        String[] toolbarTitles = new String[]{getString(R.string.title_tasks_my), getString(R.string.title_tasks_done)};
        spinner.setAdapter(new ToolbarSpinnerAdapter(toolbar.getContext(), toolbarTitles));

        String tag = "tasks_fragment";
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            fragment = TasksFragment.newInstance();
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .commit();
        activeFragment = fragment;
        activeNavItemId = R.id.nav_tasks;
    }

    private void showUsersFragment() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowCustomEnabled(false);
        ab.setDisplayShowTitleEnabled(true);
        ab.setTitle(R.string.prompt_users);

        String tag = "users_fragment";
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            fragment = UsersFragment.newInstance();
        }
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .commit();
        activeFragment = fragment;
        activeNavItemId = R.id.nav_users;
    }

    private void gotoLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void refreshActiveFragment() {
        if (activeFragment != null && activeFragment instanceof RefreshableFragment) {
            ((RefreshableFragment) activeFragment).refreshFragmentData();
        }
    }
}
