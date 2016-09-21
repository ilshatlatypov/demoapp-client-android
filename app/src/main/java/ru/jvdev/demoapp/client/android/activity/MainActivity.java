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
import android.widget.TextView;

import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.ViewSwitcher;
import ru.jvdev.demoapp.client.android.activity.task.TasksFragment;
import ru.jvdev.demoapp.client.android.activity.user.UsersFragment;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DataLoadingListener {

    private static final String STATE_ACTIVE_NAV_ITEM = "activeNavItemId";

    private ViewSwitcher viewSwitcher;
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

        viewSwitcher = new ViewSwitcher(this, R.id.progress_bar, R.id.content_frame, R.id.error_layout);
        Button retryButton = (Button) findViewById(R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActiveFragment();
            }
        });

        if (savedInstanceState == null) {
            activeNavItemId = R.id.nav_tasks;
        } else {
            activeNavItemId = savedInstanceState.getInt(STATE_ACTIVE_NAV_ITEM);
        }
        displayFragmentByNavItemId(activeNavItemId);
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

    private void setActiveFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, tag)
                .commit();
        activeFragment = fragment;
    }

    private void displayFragmentByNavItemId(@IdRes int itemId) {
        viewSwitcher.showProgressBar();
        if (itemId == R.id.nav_tasks) {
            String tag = "tasks_fragment";
            getSupportActionBar().setTitle(R.string.title_tasks);
            Fragment fragment = getFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                fragment = TasksFragment.newInstance();
            }
            setActiveFragment(fragment, tag);
            activeNavItemId = itemId;
        } else if (itemId == R.id.nav_users) {
            String tag = "users_fragment";
            getSupportActionBar().setTitle(R.string.title_users);
            Fragment fragment = getFragmentManager().findFragmentByTag(tag);
            if (fragment == null) {
                fragment = UsersFragment.newInstance();
            }
            setActiveFragment(fragment, tag);
            activeNavItemId = itemId;
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
