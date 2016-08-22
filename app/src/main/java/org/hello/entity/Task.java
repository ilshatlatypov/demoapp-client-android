package org.hello.entity;

/**
 * Created by ilshat on 04.08.16.
 */
public class Task {

    private String self;
    private String title;

    public Task() {
    }

    public Task(String title) {
        this.title = title;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
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
