package ru.yandex.javacource.aldukhov.schedule.task;

import ru.yandex.javacource.aldukhov.schedule.manager.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Integer> subTaskIds;
    private LocalDateTime endTime;

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

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTimeFields(TaskManager taskManager) {
        if (subTaskIds.isEmpty()) {
            setDuration(Duration.ZERO);
            setStartTime(null);
            endTime = null;
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Integer subtaskId : subTaskIds) {
            Subtask subtask = taskManager.subtaskById(subtaskId);
            if (subtask != null && subtask.getStartTime() != null) {
                if (earliestStart == null || subtask.getStartTime().isBefore(earliestStart)) {
                    earliestStart = subtask.getStartTime();
                }
                LocalDateTime subtaskEnd = subtask.getEndTime();
                if (latestEnd == null || subtaskEnd.isAfter(latestEnd)) {
                    latestEnd = subtaskEnd;
                }
                totalDuration = totalDuration.plus(subtask.getDuration());
            }
        }

        setStartTime(earliestStart);
        setDuration(totalDuration);
        endTime = latestEnd;
    }
}