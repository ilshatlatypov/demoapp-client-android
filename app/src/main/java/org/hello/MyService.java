package org.hello;

import org.hello.entity.User;
import org.hello.entity.dto.UsersPageDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by ilshat on 24.08.16.
 */
public interface MyService {

    @GET("users")
    Call<UsersPageDto> listUsers();

    @GET("users/{id}")
    Call<User> getUser(@Path("id") int id);
}
