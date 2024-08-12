package ru.yandex.javacource.aldukhov.schedule.manager;

public class ManagerSaveException extends RuntimeException {
    public  ManagerSaveException(String massage, Throwable cause) {
        super(massage, cause);
    }
}
