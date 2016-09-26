package ru.jvdev.demoapp.client.android.activity;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import ru.jvdev.demoapp.client.android.DemoApp;
import ru.jvdev.demoapp.client.android.R;
import ru.jvdev.demoapp.client.android.activity.task.TasksFragment;
import ru.jvdev.demoapp.client.android.activity.user.UsersFragment;
import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.utils.KeyboardUtils;

import static ru.jvdev.demoapp.client.android.activity.task.TasksFragment.TASKS_ALL_DONE;
import static ru.jvdev.demoapp.client.android.activity.task.TasksFragment.TASKS_ALL_TODO;
import static ru.jvdev.demoapp.client.android.activity.task.TasksFragment.TASKS_CURRENT_DONE;
import static ru.jvdev.demoapp.client.android.activity.task.TasksFragment.TASKS_CURRENT_TODO;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String STATE_ACTIVE_NAV_ITEM = "activeNavItemId";

    private Fragment activeFragment;
    private int activeNavItemId;

    private User currentUser;

    private ActionBarDrawerToggle drawerToggle;

    private Menu menu;

    private ToolbarState toolbarState;
    private boolean inSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (inSearchMode) {
                    exitSearchMode();
                }
            }
        });
        drawerToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        User activeUser = ((DemoApp) getApplicationContext()).getActiveUser();
        ((TextView) header.findViewById(R.id.username)).setText(activeUser.getFullname());

        if (activeUser.getRole() == Role.EMPLOYEE) {
            navigationView.getMenu().findItem(R.id.nav_users).setVisible(false);
        }

        currentUser = ((DemoApp) getApplicationContext()).getActiveUser();

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
        if (inSearchMode) {
            exitSearchMode();
        } else {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshActiveFragment();
        } else if (id == R.id.action_search) {
            enterSearchMode();
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
        Spinner spinner = (Spinner) LayoutInflater.from(ab.getThemedContext()).inflate(R.layout.toolbar_spinner, null);
        setActionBarCustomView(ab, spinner);

        String[] toolbarTitles;
        final int[] taskFilters;
        if (currentUser.getRole() == Role.MANAGER) {
            toolbarTitles = new String[]{
                    getString(R.string.title_tasks_all_todo),
                    getString(R.string.title_tasks_all_done),
                    getString(R.string.title_tasks_my_todo),
                    getString(R.string.title_tasks_my_done)
            };
            taskFilters = new int[]{
                    TASKS_ALL_TODO,
                    TASKS_ALL_DONE,
                    TASKS_CURRENT_TODO,
                    TASKS_CURRENT_DONE
            };
        } else {
            toolbarTitles = new String[]{
                    getString(R.string.title_tasks_my_todo),
                    getString(R.string.title_tasks_my_done)
            };
            taskFilters = new int[]{
                    TASKS_CURRENT_TODO,
                    TASKS_CURRENT_DONE
            };
        }

        spinner.setAdapter(new ToolbarSpinnerAdapter(spinner.getContext(), toolbarTitles));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String tag = "tasks_fragment";
                int tasksFilter = taskFilters[position];
                Fragment fragment = TasksFragment.newInstance(tasksFilter);
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, tag)
                        .commit();
                activeFragment = fragment;
                activeNavItemId = R.id.nav_tasks;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
    }

    private void showUsersFragment() {
        ActionBar ab = getSupportActionBar();
        setActionBarTitle(ab, R.string.prompt_users);

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

    private void enterSearchMode() {
        ActionBar ab = getSupportActionBar();

        View view = LayoutInflater.from(ab.getThemedContext()).inflate(R.layout.toolbar_search, null);

        final EditText searchText = (EditText) view.findViewById(R.id.text_search);
        final ImageButton clearSearchButton = (ImageButton) view.findViewById(R.id.clear_search_button);

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence text, int i, int i1, int i2) {
                clearSearchButton.setVisibility(text.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        clearSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchText.setText("");
            }
        });

        toolbarState = new ToolbarState();
        if (ab.getCustomView() != null) {
            toolbarState.setCustomViewState(ab.getCustomView());
        } else {
            toolbarState.setTitleState(ab.getTitle());
            ab.setDisplayShowTitleEnabled(false);
        }
        ab.setCustomView(view);
        ab.setDisplayShowCustomEnabled(true);

        drawerToggle.setDrawerIndicatorEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);

        searchText.requestFocus();
        KeyboardUtils.showKeyboard(MainActivity.this);
        menu.findItem(R.id.action_search).setVisible(false);

        inSearchMode = true;
    }

    private void exitSearchMode() {
        ActionBar ab = getSupportActionBar();

        if (toolbarState.isCustomViewState()) {
            ab.setCustomView(toolbarState.getCustomView());
        } else {
            setActionBarTitle(ab, toolbarState.getTitle());
        }

        ab.setDisplayHomeAsUpEnabled(false);
        drawerToggle.setDrawerIndicatorEnabled(true);

        menu.findItem(R.id.action_search).setVisible(true);
        KeyboardUtils.hideKeyboard(MainActivity.this);
        inSearchMode = false;
    }

    private static void setActionBarTitle(ActionBar ab, CharSequence title) {
        ab.setDisplayShowCustomEnabled(false);
        ab.setCustomView(null);
        ab.setTitle(title);
        ab.setDisplayShowTitleEnabled(true);
    }

    private static void setActionBarTitle(ActionBar ab, @StringRes int titleRes) {
        ab.setDisplayShowCustomEnabled(false);
        ab.setCustomView(null);
        ab.setTitle(titleRes);
        ab.setDisplayShowTitleEnabled(true);
    }

    private static void setActionBarCustomView(ActionBar ab, View view) {
        ab.setDisplayShowTitleEnabled(false);
        ab.setTitle(null);
        ab.setCustomView(view);
        ab.setDisplayShowCustomEnabled(true);
    }

    private static class ToolbarState {
        private View customView;
        private CharSequence title;

        public void setCustomViewState(View customView) {
            this.customView = customView;
            this.title = null;
        }

        public void setTitleState(CharSequence title) {
            this.title = title;
            this.customView = null;
        }

        public boolean isCustomViewState() {
            return customView != null;
        }

        public View getCustomView() {
            return customView;
        }

        public CharSequence getTitle() {
            return title;
        }
    }
}
