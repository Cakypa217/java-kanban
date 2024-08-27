package ru.yandex.javacource.aldukhov.schedule.task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, Status status, Type type, int epicId) {
        super(id, name, description, status, type);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public Type getType() {
        return Type.SUBTASK;
    }
}