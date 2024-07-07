package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.util.List;

public interface TaskManager {
    int addNewTask(Task task);

    Integer addNewEpic(Epic epic);

    Integer addNewSubtask(Subtask subtask);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubtask();

    List<Subtask> getTaskOfEpic(Epic epic);

    void clearTask();

    void clearEpic();

    void clearSubtasks();

    Task taskById(int id);

    Epic epicById(int id);

    Subtask subtaskById(int id);

    void delTaskById(int id);

    void delEpicById(int id);

    void delSubtaskById(int id);

    List<Task> getHistory();
}
