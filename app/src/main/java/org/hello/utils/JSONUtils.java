package org.hello.utils;

import org.hello.entity.Task;
import org.hello.entity.User;
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

    public static User parseAsUser(String jsonStr) {
        User user = new User();
        try {
            JSONObject json = new JSONObject(jsonStr);
            user.setFirstname(json.getString("firstname"));
            user.setLastname(json.getString("lastname"));
        } catch (JSONException e) {
            // TODO handle this
        }
        return user;
    }

    public static List<User> parseAsUsersList(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        JSONArray peopleJsonArray = json.getJSONObject("_embedded").getJSONArray("users");
        List<User> people = new ArrayList<>();
        for (int i = 0; i < peopleJsonArray.length(); i++) {
            JSONObject userJson = peopleJsonArray.getJSONObject(i);
            User user = new User();

            String selfLink = userJson.getJSONObject("_links").getJSONObject("self").getString("href");
            user.setSelf(selfLink);
            user.setFirstname(userJson.getString("firstname"));
            user.setLastname(userJson.getString("lastname"));

            people.add(user);
        }
        return people;
    }

    public static List<Task> parseAsTasksList(String jsonStr) throws JSONException {
        JSONObject json = new JSONObject(jsonStr);
        JSONArray peopleJsonArray = json.getJSONObject("_embedded").getJSONArray("tasks");
        List<Task> tasks = new ArrayList<>();
        for (int i = 0; i < peopleJsonArray.length(); i++) {
            JSONObject taskJson = peopleJsonArray.getJSONObject(i);
            Task task = new Task();

            String selfLink = taskJson.getJSONObject("_links").getJSONObject("self").getString("href");
            task.setSelf(selfLink);
            task.setTitle(taskJson.getString("title"));

            tasks.add(task);
        }
        return tasks;
    }

    public static JSONObject toJSON(User user) {
        JSONObject json = new JSONObject();
        try {
            json.put("firstname", user.getFirstname());
            json.put("lastname", user.getLastname());
            json.put("username", user.getUsername());
            json.put("password", user.getPassword());
        } catch (JSONException e) {
            // TODO handle this
        }
        return json;
    }
}
