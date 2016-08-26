package org.hello.entity.dto;

import com.google.gson.annotations.SerializedName;

import org.hello.entity.User;

/**
 * Created by ilshat on 24.08.16.
 */
public class UserDto {
    private String firstname;
    private String lastname;
    private String username;
    private String password;
    @SerializedName("_links")
    private Links links;

    public UserDto(User user) {
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.username = user.getUsername();
        this.password = user.getPassword();
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

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public User toUser() {
        User user = new User(firstname, lastname, username, password);
        user.setSelf(links.getSelf().getHref());
        return user;
    }
}
