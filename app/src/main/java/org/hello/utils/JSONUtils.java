package org.hello.utils;

import org.hello.entity.Task;
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
}
