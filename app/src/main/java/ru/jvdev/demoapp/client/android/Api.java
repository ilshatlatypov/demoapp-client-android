package ru.jvdev.demoapp.client.android;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.jvdev.demoapp.client.android.entity.dto.TaskDto;
import ru.jvdev.demoapp.client.android.entity.dto.TasksPageDto;
import ru.jvdev.demoapp.client.android.entity.dto.UserDto;
import ru.jvdev.demoapp.client.android.entity.dto.UsersPageDto;

/**
 * Created by ilshat on 24.08.16.
 */
public class Api {

    public interface Users {
        @GET("users?sort=firstname&sort=lastname")
        Call<UsersPageDto> getUsers();

        @POST("users")
        Call<Void> createUser(@Body UserDto user);

        @PUT("users/{id}")
        Call<Void> updateUser(@Path("id") int id, @Body UserDto user);

        @GET("users/{id}")
        Call<UserDto> getUser(@Path("id") int id);

        @GET("users/search/findByUsername")
        Call<UserDto> getUserByUsername(@Query("username") String username);

        @DELETE("users/{id}")
        Call<Void> deleteUser(@Path("id") int id);

    }

    public interface Tasks {
        @GET("tasks?sort=date")
        Call<TasksPageDto> list();

        @POST("tasks")
        Call<Void> create(@Body TaskDto task);

        @PUT("tasks/{id}")
        Call<Void> update(@Path("id") int id, @Body TaskDto task);

        @GET("tasks/{id}")
        Call<TaskDto> get(@Path("id") int id);
    }
}
