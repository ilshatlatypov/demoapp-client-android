package ru.jvdev.demoapp.client.android.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ilshat on 04.08.16.
 */
public class Task implements Serializable {

    private int id;
    private String title;
    private Date date;
    private boolean done;
    private User user;

    public Task() {
    }

    public Task(String title, Date date) {
        this.title = title;
        this.date = date;
    }

    public Task(int id, String title) {
        this.id = id;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String toString() {
        return title;
    }
}
