package ru.jvdev.demoapp.client.android.entity.dto;

import ru.jvdev.demoapp.client.android.entity.Role;
import ru.jvdev.demoapp.client.android.entity.User;

/**
 * Created by ilshat on 24.08.16.
 */
public class UserDto {

    private int id;
    private String firstname;
    private String lastname;
    private String username;
    private Role role;

    public UserDto(User user) {
        this.id = user.getId();
        this.firstname = user.getFirstname();
        this.lastname = user.getLastname();
        this.username = user.getUsername();
        this.role = user.getRole();
    }

    public User toUser() {
        User user = new User(firstname, lastname, username, role);
        user.setId(id);
        return user;
    }

    public int getId() {
        return id;
    }
}
