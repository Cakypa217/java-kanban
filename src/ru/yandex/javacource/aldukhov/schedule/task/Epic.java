package ru.yandex.javacource.aldukhov.schedule.task;


import java.time.Duration;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTaskIds;

    public Epic(String name, String description) {
        super(name, description, Duration.ZERO, null);
        this.subTaskIds = new ArrayList<>();
    }

    public Epic(int id, String name, String description, Status status, Type type) {
        super(id, name, description, status, type);
        this.subTaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void setSubTaskIds(ArrayList<Integer> subTaskIds) {
        this.subTaskIds = subTaskIds;
    }

    public void addSubtaskId(Integer subtaskId) {
        if (!subTaskIds.contains(subtaskId)) {
            subTaskIds.add(subtaskId);
        }
    }

    public void cleanSubtaskIds() {
        subTaskIds.clear();
    }

    @Override
    public Type getType() {
        return Type.EPIC;
    }
}