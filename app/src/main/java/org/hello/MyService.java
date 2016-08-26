package org.hello;

import org.hello.entity.dto.UserDto;
import org.hello.entity.dto.UsersPageDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by ilshat on 24.08.16.
 */
public interface MyService {

    @GET("users")
    Call<UsersPageDto> listUsers();

    @POST("users")
    Call<Void> createUser(@Body UserDto user);

    @GET("users/{id}")
    Call<UserDto> getUser(@Path("id") int id);
}
