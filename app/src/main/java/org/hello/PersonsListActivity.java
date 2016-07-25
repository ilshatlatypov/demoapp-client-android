package org.hello;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class PersonsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int ADD_PERSON_REQUEST = 1;
    private static final int PERSON_DETAILS_REQUEST = 2;
    public static final String EXTRA_PERSON = "person";

    private ViewSwitcherNew viewSwitcher;
    private ListView lvPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAddPersonActivity();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        viewSwitcher = new ViewSwitcherNew(this, R.id.progress_bar, R.id.main_layout, R.id.error_layout);

        ArrayAdapter<Person> peopleListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvPeople = (ListView) findViewById(R.id.lv_people);
        lvPeople.setAdapter(peopleListAdapter);
        lvPeople.setEmptyView(findViewById(android.R.id.empty));

        lvPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Person person = (Person) parent.getItemAtPosition(position);
                openPersonDetailsActivity(person);
            }
        });

        Button buttonRetry = (Button) findViewById(R.id.button_retry);
        buttonRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePeopleList();
            }
        });

        Button buttonAddPerson = (Button) findViewById(R.id.button_create_person_from_empty_list);
        buttonAddPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAddPersonActivity();
            }
        });

        updatePeopleList();
    }

    private void openAddPersonActivity() {
        Intent intent = new Intent(this, AddPersonActivity.class);
        this.startActivityForResult(intent, ADD_PERSON_REQUEST);
    }

    private void openPersonDetailsActivity(Person person) {
        Intent intent = new Intent(this, PersonDetailsActivity.class);
        intent.putExtra(EXTRA_PERSON, person);
        this.startActivityForResult(intent, PERSON_DETAILS_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ADD_PERSON_REQUEST) {
            if (resultCode == RESULT_OK) {
                updatePeopleList();
            }
        } else if (requestCode == PERSON_DETAILS_REQUEST) {
            if (resultCode == PersonDetailsActivity.RESULT_PERSON_DELETED) {
                Snackbar.make(lvPeople, R.string.prompt_person_deleted, Snackbar.LENGTH_SHORT).show();
                updatePeopleList();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updatePeopleList() {
        viewSwitcher.showProgressBar();
        new UpdatePersonsListTask().execute();
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

        } else if (id == R.id.action_refresh) {
            updatePeopleList();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    enum TaskResultType {
        SUCCESS,
        UNEXPECTED_RESPONSE_CODE,
        SERVER_UNAVAILABLE,
        NO_CONNECTION
    }

    private class TaskResult {
        TaskResultType resultType;
        List<Person> persons;

        public TaskResult(TaskResultType resultType) {
            this.resultType = resultType;
        }

        public TaskResult(List<Person> persons) {
            this.resultType = TaskResultType.SUCCESS;
            this.persons = persons;
        }
    }

    private class UpdatePersonsListTask extends AsyncTask<Void, Void, TaskResult> {

        @Override
        protected TaskResult doInBackground(Void... params) {
            if (!ConnectionUtils.isConnected(PersonsListActivity.this)) {
                return new TaskResult(TaskResultType.NO_CONNECTION);
            }

            try {
                ResponseEntity<String> responseEntity = RestUtils.getPersonsList();
                HttpStatus httpStatus = responseEntity.getStatusCode();
                if (httpStatus == HttpStatus.OK) {
                    List<Person> persons = parseAsPersonsList(responseEntity.getBody());
                    return new TaskResult(persons);
                } else {
                    return new TaskResult(TaskResultType.UNEXPECTED_RESPONSE_CODE);
                }
            } catch (Exception e) {
                return new TaskResult(TaskResultType.SERVER_UNAVAILABLE);
            }
        }

        @NonNull
        private List<Person> parseAsPersonsList(String jsonStr) throws JSONException {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray peopleJsonArray = json.getJSONObject("_embedded").getJSONArray("people");
            List<Person> people = new ArrayList<>();
            for (int i = 0; i < peopleJsonArray.length(); i++) {
                JSONObject personJson = peopleJsonArray.getJSONObject(i);
                Person person = new Person();
                person.setFirstName(personJson.getString("firstName"));
                person.setLastName(personJson.getString("lastName"));
                String selfLink = personJson.getJSONObject("_links").getJSONObject("self").getString("href");
                person.setSelfLink(selfLink);
                people.add(person);
            }
            return people;
        }

        @Override
        protected void onPostExecute(TaskResult taskResult) {

            if (taskResult.resultType == TaskResultType.SUCCESS) {
                ArrayAdapter<Person> peopleListAdapter = (ArrayAdapter<Person>) lvPeople.getAdapter();
                peopleListAdapter.clear();
                peopleListAdapter.addAll(taskResult.persons);
                peopleListAdapter.notifyDataSetChanged();
                viewSwitcher.showMainLayout();
                return;
            }

            CharSequence errorMessage = null;
            switch (taskResult.resultType) {
                case UNEXPECTED_RESPONSE_CODE:
                    errorMessage = getText(R.string.error_unexpected_response);
                    break;
                case SERVER_UNAVAILABLE:
                    errorMessage = getText(R.string.error_server_unavailable);
                    break;
                case NO_CONNECTION:
                    errorMessage = getText(R.string.error_no_connection);
                    break;
            }
            TextView errorTextView = (TextView) findViewById(R.id.error_text);
            errorTextView.setText(errorMessage);
            viewSwitcher.showErrorLayout();
        }
    }
}
