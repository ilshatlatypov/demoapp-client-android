package ru.jvdev.demoapp.client.android.entity;

/**
 * Created by ilshat on 30.08.16.
 */
public enum Role {
    NO_ROLE("Должность"),
    MANAGER("Менеджер"),
    EMPLOYEE("Рабочий");

    private String title;

    Role(String title) {
        this.title = title;
    }

    public String toString() {
        return title;
    }
}
