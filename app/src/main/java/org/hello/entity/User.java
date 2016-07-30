package org.hello.entity;

import java.io.Serializable;

/**
 * Created by ilshat on 17.07.16.
 */
public class User implements Serializable {

    private String selfLink;
    private String firstname;
    private String lastname;

    public User() {}

    public User(String firstname, String lastName) {
        this.firstname = firstname;
        this.lastname = lastName;
    }

    public String getSelfLink() {
        return selfLink;
    }

    public void setSelfLink(String selfLink) {
        this.selfLink = selfLink;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String toString() {
        return firstname + " " + lastname;
    }
}
