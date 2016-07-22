package org.hello;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class PersonsListActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int CREATE_PERSON_REQUEST = 1;

    private SwipeRefreshLayout srlPeople;
    private ListView lvPeople;
    private ProgressBarSwitcher progressBarSwitcher;

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
                openCreatePersonActivity();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        progressBarSwitcher = new ProgressBarSwitcher(this, R.id.pb_people, R.id.srl_people);

        srlPeople = (SwipeRefreshLayout) findViewById(R.id.srl_people);
        srlPeople.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updatePeopleList();
            }
        });

        ArrayAdapter<Person> peopleListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        lvPeople = (ListView) findViewById(R.id.lv_people);
        lvPeople.setAdapter(peopleListAdapter);

        progressBarSwitcher.showProgress(true);
        updatePeopleList();
    }

    private void openCreatePersonActivity() {
        Intent intent = new Intent(this, CreatePersonActivity.class);
        this.startActivityForResult(intent, CREATE_PERSON_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_PERSON_REQUEST) {
            if (resultCode == RESULT_OK) {
                progressBarSwitcher.showProgress(true);
                updatePeopleList();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updatePeopleList() {
        new UpdatePeopleListTask().execute();
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

    private class UpdatePeopleListTask extends AsyncTask<Void, Void, List<Person>> {

        @Override
        protected List<Person> doInBackground(Void... params) {
            try {
                final String url = "http://192.168.2.11:8080/people";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        url, HttpMethod.GET, null,
                        String.class);
                return parseAsPeople(responseEntity.getBody());
            } catch (Exception e) {
                Log.e("PersonsListActivity", e.getMessage(), e);
            }

            return null;
        }

        @NonNull
        private List<Person> parseAsPeople(String jsonStr) throws JSONException {
            JSONObject json = new JSONObject(jsonStr);
            JSONArray peopleJsonArray = json.getJSONObject("_embedded").getJSONArray("people");
            List<Person> people = new ArrayList<>();
            for (int i = 0; i < peopleJsonArray.length(); i++) {
                JSONObject personJson = peopleJsonArray.getJSONObject(i);
                Person person = new Person();
                person.setFirstName(personJson.getString("firstName"));
                person.setLastName(personJson.getString("lastName"));
                people.add(person);
            }
            return people;
        }

        @Override
        protected void onPostExecute(List<Person> people) {
            if (people == null) {
                return; // TODO show connection error
            }

            ArrayAdapter<Person> peopleListAdapter = (ArrayAdapter<Person>) lvPeople.getAdapter();
            peopleListAdapter.clear();
            peopleListAdapter.addAll(people);
            peopleListAdapter.notifyDataSetChanged();

            progressBarSwitcher.showProgress(false);
            srlPeople.setRefreshing(false);
        }
    }
}
