package org.hello.entity.dto;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilshat on 24.08.16.
 */
public class UsersPageDto {

    @SerializedName("_embedded")
    private Embedded embedded;

    public List<UserDto> getUsers() {
        return embedded.users;
    }

    public class Embedded {
        private List<UserDto> users = new ArrayList<>();
    }
}