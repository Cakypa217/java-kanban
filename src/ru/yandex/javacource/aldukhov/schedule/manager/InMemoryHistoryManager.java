package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    private static  final int MAX_HISTORY_SIZE = 9;
    ArrayList<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(history);
    }
}
