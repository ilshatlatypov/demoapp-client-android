package org.hello;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
        lvPeople.setEmptyView(findViewById(android.R.id.empty));

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
        NOT_CONNECTED
    }

    private class UpdatePersonsListTaskResult {
        TaskResultType resultType;
        List<Person> persons;

        public UpdatePersonsListTaskResult(TaskResultType resultType) {
            this.resultType = resultType;
        }

        public UpdatePersonsListTaskResult(List<Person> persons) {
            this.resultType = TaskResultType.SUCCESS;
            this.persons = persons;
        }
    }

    private class UpdatePersonsListTask extends AsyncTask<Void, Void, UpdatePersonsListTaskResult> {

        @Override
        protected UpdatePersonsListTaskResult doInBackground(Void... params) {
            try {
                final String url = "http://192.168.2.11:8080/people";
                RestTemplate restTemplate = new RestTemplate();
                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                ResponseEntity<String> responseEntity = restTemplate.exchange(
                        url, HttpMethod.GET, null,
                        String.class);
                HttpStatus httpStatus = responseEntity.getStatusCode();
                if (httpStatus == HttpStatus.OK) {
                    List<Person> persons = parseAsPersonsList(responseEntity.getBody());
                    return new UpdatePersonsListTaskResult(persons);
                } else {
                    return new UpdatePersonsListTaskResult(TaskResultType.UNEXPECTED_RESPONSE_CODE);
                }
            } catch (Exception e) {
                if (checkConnection(PersonsListActivity.this)) {
                    return new UpdatePersonsListTaskResult(TaskResultType.SERVER_UNAVAILABLE);
                } else {
                    return new UpdatePersonsListTaskResult(TaskResultType.NOT_CONNECTED);
                }
                // Log.e("PersonsListActivity", e.getMessage(), e);
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
                people.add(person);
            }
            return people;
        }

        @Override
        protected void onPostExecute(UpdatePersonsListTaskResult taskResult) {
            progressBarSwitcher.showProgress(false);
            srlPeople.setRefreshing(false);

            switch (taskResult.resultType) {
                case SUCCESS:
                    ArrayAdapter<Person> peopleListAdapter = (ArrayAdapter<Person>) lvPeople.getAdapter();
                    peopleListAdapter.clear();
                    peopleListAdapter.addAll(taskResult.persons);
                    peopleListAdapter.notifyDataSetChanged();
                    break;
                case UNEXPECTED_RESPONSE_CODE:
                    // TODO show view "Something is wrong with the request you send." + Retry
                    break;
                case SERVER_UNAVAILABLE:
                    // TODO show v iew "Server is not responding. Please try to send the request later." + Retry
                    break;
                case NOT_CONNECTED:
                    // TODO show view "Error on data. Check your network connection." + Retry
                    break;
            }
        }
    }

    public static boolean checkConnection(Context ctx) {
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
