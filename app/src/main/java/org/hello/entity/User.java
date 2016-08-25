package org.hello.entity;

import java.io.Serializable;

/**
 * Created by ilshat on 17.07.16.
 */
public class User implements Serializable {

    private String self;
    private String firstname;
    private String lastname;
    private String username;
    private String password;

    public User() {}

    public User(String firstname, String lastName, String username, String password) {
        this.firstname = firstname;
        this.lastname = lastName;
        this.username = username;
        this.password = password;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return firstname + " " + lastname;
    }
}
