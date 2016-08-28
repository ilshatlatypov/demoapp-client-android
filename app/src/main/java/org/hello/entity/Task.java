package org.hello.entity;

/**
 * Created by ilshat on 04.08.16.
 */
public class Task {

    private int id;
    private String title;

    public Task() {
    }

    public Task(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return title;
    }
}