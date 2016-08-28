package ru.jvdev.demoapp.client.android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
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

import ru.jvdev.demoapp.client.android.ViewSwitcher;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentDataLoadingListener {

    private ViewSwitcher viewSwitcher;
    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ru.jvdev.R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(ru.jvdev.R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(ru.jvdev.R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, ru.jvdev.R.string.navigation_drawer_open, ru.jvdev.R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(ru.jvdev.R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewSwitcher = new ViewSwitcher(this, ru.jvdev.R.id.progress_bar, ru.jvdev.R.id.content_frame, ru.jvdev.R.id.error_layout);
        Button retryButton = (Button) findViewById(ru.jvdev.R.id.button_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActiveFragment();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(ru.jvdev.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(ru.jvdev.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == ru.jvdev.R.id.action_refresh) {
            refreshActiveFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == ru.jvdev.R.id.nav_users || id == ru.jvdev.R.id.nav_tasks) {
            displayFragmentByNavItemId(id);
        } else if (id == ru.jvdev.R.id.nav_gallery) {

        } else if (id == ru.jvdev.R.id.nav_slideshow) {

        } else if (id == ru.jvdev.R.id.nav_manage) {

        } else if (id == ru.jvdev.R.id.nav_share) {

        } else if (id == ru.jvdev.R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(ru.jvdev.R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setActiveFragment(android.app.Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(ru.jvdev.R.id.content_frame, fragment)
                .commit();
        activeFragment = fragment;
    }

    private void displayFragmentByNavItemId(@IdRes int itemId) {
        viewSwitcher.showProgressBar();
        if (itemId == ru.jvdev.R.id.nav_users) {
            getSupportActionBar().setTitle(ru.jvdev.R.string.title_users);
            setActiveFragment(UsersFragment.newInstance());
        } else if (itemId == ru.jvdev.R.id.nav_tasks) {
            getSupportActionBar().setTitle(ru.jvdev.R.string.title_tasks);
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
        TextView errorTextView = (TextView) findViewById(ru.jvdev.R.id.error_text);
        errorTextView.setText(errorMessage);
        viewSwitcher.showErrorLayout();
    }
}
