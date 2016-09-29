package ru.jvdev.demoapp.client.android.entity;

import java.io.Serializable;

/**
 * Created by ilshat on 17.07.16.
 */
public class User implements Serializable {

    private int id;
    private String firstname;
    private String lastname;
    private String username;
    private Role role;

    public User() {}

    public User(String firstname) {
        this.firstname = firstname;
        this.role = Role.NO_ROLE;
    }

    public User(String firstname, String lastName, String username, Role role) {
        this.firstname = firstname;
        this.lastname = lastName;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFullname() {
        if (lastname != null && !lastname.isEmpty())
            return firstname + " " + lastname;
        else
            return firstname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String toString() {
        return getFullname();
    }
}
