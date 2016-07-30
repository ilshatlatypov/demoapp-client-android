package org.hello.utils;

import org.hello.entity.Person;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilshat on 25.07.16.
 */
public class JSONUtils {

    private JSONUtils() {}

    public static Person parseAsPerson(String jsonStr) {
        Person person = new Person();
        try {
            JSONObject json = new JSONObject(jsonStr);
            person.setFirstName(json.getString("firstName"));
            person.setLastName(json.getString("lastName"));
        } catch (JSONException e) {
            // TODO handle this
        }
        return person;
    }

    public static List<Person> parseAsPersonsList(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        JSONArray peopleJsonArray = json.getJSONObject("_embedded").getJSONArray("people");
        List<Person> people = new ArrayList<>();
        for (int i = 0; i < peopleJsonArray.length(); i++) {
            JSONObject personJson = peopleJsonArray.getJSONObject(i);
            Person person = new Person();

            String selfLink = personJson.getJSONObject("_links").getJSONObject("self").getString("href");
            person.setSelfLink(selfLink);
            person.setFirstName(personJson.getString("firstName"));
            person.setLastName(personJson.getString("lastName"));

            people.add(person);
        }
        return people;
    }

    public static JSONObject toJSON(Person person) {
        JSONObject json = new JSONObject();
        try {
            json.put("firstName", person.getFirstName());
            json.put("lastName", person.getLastName());
        } catch (JSONException e) {
            // TODO handle this
        }
        return json;
    }
}
