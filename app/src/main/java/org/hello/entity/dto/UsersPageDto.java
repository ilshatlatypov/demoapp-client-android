package org.hello.entity.dto;

import com.google.gson.annotations.SerializedName;

import org.hello.entity.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilshat on 24.08.16.
 */
public class UsersPageDto {

    @SerializedName("_embedded")
    private Embedded embedded;

    public List<User> getUsers() {
        List<User> users = new ArrayList<>(embedded.users.size());
        for (UserDto userDto : embedded.users) {
            users.add(userDto.toUser());
        }
        return users;
    }

    public class Embedded {
        private List<UserDto> users = new ArrayList<>();
    }
}