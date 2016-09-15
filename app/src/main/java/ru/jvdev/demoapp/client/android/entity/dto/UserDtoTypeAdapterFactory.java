package ru.jvdev.demoapp.client.android.entity.dto;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

import ru.jvdev.demoapp.client.android.utils.StringUtils;

/**
 * Created by ilshat on 15.09.16.
 */
public class UserDtoTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, type);
        return new TypeAdapter<T>() {
            public void write(JsonWriter out, T value) throws IOException {
                int userId = ((UserDto) value).getId();
                String userUri = StringUtils.buildURIFromId(userId);
                out.value(userUri);
            }
            public T read(JsonReader in) throws IOException {
                return delegate.read(in);
            }
        };
    }
}
