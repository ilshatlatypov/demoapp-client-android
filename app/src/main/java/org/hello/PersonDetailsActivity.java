package org.hello;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class PersonDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Person person = (Person) getIntent().getSerializableExtra(PersonsListActivity.EXTRA_PERSON);
        ((TextView) findViewById(R.id.firstname)).setText(person.getFirstName());
        ((TextView) findViewById(R.id.lastname)).setText(person.getLastName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
