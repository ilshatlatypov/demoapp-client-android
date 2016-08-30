package ru.jvdev.demoapp.client.android.entity;

/**
 * Created by ilshat on 30.08.16.
 */
public enum Role {
    NO_ROLE(0, "Должность"),
    ADMIN(1, "Администратор"),
    DIRECTOR(2, "Директор"),
    MANAGER(3, "Менеджер"),
    EMPLOYEE(4, "Рабочий");

    private int id;
    private String title;

    Role(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public String toString() {
        return title;
    }
}
