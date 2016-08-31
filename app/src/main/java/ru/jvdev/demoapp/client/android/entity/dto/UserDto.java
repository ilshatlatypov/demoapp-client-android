package ru.jvdev.demoapp.client.android.entity.dto;

import com.google.gson.annotations.SerializedName;

import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.User;
import ru.jvdev.demoapp.client.android.utils.StringUtils;

/**
 * Created by ilshat on 24.08.16.
 */
public class UserDto {
    private String firstname;
    private String lastname;
    private String username;
    private String password;
    private Role role;
    @SerializedName("_links")
    private Links links;

    public UserDto(User user) {
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.role = user.getRole();
    }

    public User toUser() {
        User user = new User(firstname, lastname, username, password, role);
        user.setId(StringUtils.getIdFromURL(links.getSelf().getHref()));
        return user;
    }
}
