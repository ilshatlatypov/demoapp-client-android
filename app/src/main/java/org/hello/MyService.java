package org.hello;

import org.hello.entity.dto.TasksPageDto;
import org.hello.entity.dto.UserDto;
import org.hello.entity.dto.UsersPageDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by ilshat on 24.08.16.
 */
public interface MyService {

    @GET("users")
    Call<UsersPageDto> getUsers();

    @POST("users")
    Call<Void> createUser(@Body UserDto user);

    @PUT("users/{id}")
    Call<Void> updateUser(@Path("id") int id, @Body UserDto user);

    @GET("users/{id}")
    Call<UserDto> getUser(@Path("id") int id);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int id);

    @GET("tasks")
    Call<TasksPageDto> getTasks();
}