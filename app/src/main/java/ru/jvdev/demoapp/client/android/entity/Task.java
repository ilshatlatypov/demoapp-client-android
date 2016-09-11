package ru.jvdev.demoapp.client.android.entity;

import java.util.Date;

/**
 * Created by ilshat on 04.08.16.
 */
public class Task {

    private int id;
    private String title;
    private Date date;

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

    public String toString() {
        return title;
    }
}
