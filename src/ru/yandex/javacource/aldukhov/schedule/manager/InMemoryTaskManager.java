package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected int generatorId = 0;
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();


    @Override
    public int addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public Integer addNewEpic(Epic epic) {
        for (Integer subtaskId : epic.getSubTaskIds()) {
            if (subtaskId == epic.getId()) {
                return null;
            }
        }
        Integer id = ++generatorId;
        epic.setId(id);
        epics.put(id, epic);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        final int id = ++generatorId;
        if (id == epicId) {
            return null;
        }
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epic);
        return id;
    }

    protected void addAnyTask(Task task) {
        final int id = task.getId();
        switch (task.getType()) {
            case TASK:
                tasks.put(id, task);
                break;
            case SUBTASK:
                subtasks.put(id, (Subtask) task);
                break;
            case EPIC:
                epics.put(id, (Epic) task);
                break;
        }
    }

    @Override
    public void updateTask(Task task) {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        tasks.put(id, task);
    }

    @Override
    public void updateEpic(Epic epic) {
        final Epic savedEpic = epics.get(epic.getId());
        if (savedEpic == null) {
            return;
        }
        epic.setSubTaskIds(savedEpic.getSubTaskIds());
        epic.setStatus(savedEpic.getStatus());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        int epicId = subtask.getEpicId();
        final int id = subtask.getId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        final Subtask saveSubtask = subtasks.get(id);
        if (saveSubtask == null) {
            return;
        }
        subtasks.put(id, subtask);
        updateEpicStatus(epic);
    }

    private void updateEpicStatus(Epic epic) {
        boolean allDone = true;
        boolean allNew = true;
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        for (Integer subTaskId : epic.getSubTaskIds()) {
            Subtask subtask = subtasks.get(subTaskId);
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }
        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubtask() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getTaskOfEpic(Epic epic) {
        List<Subtask> subTaskOfEpic = new ArrayList<>();
        for (int id : epic.getSubTaskIds()) {
            Subtask subtask = subtasks.get(id);
            if (subtask != null) {
                subTaskOfEpic.add(subtask);
            }
        }
        return subTaskOfEpic;
    }

    @Override
    public void clearTask() {
        tasks.clear();
    }

    @Override
    public void clearEpic() {
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpicStatus(epic);
        }
        subtasks.clear();
    }

    @Override
    public Task taskById(int id) {
        Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic epicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask subtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public void delTaskById(int id) {
        if (!tasks.containsKey(id)) {
            return;
        }
        tasks.remove(id);
    }

    @Override
    public void delEpicById(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        Epic epicTask = epics.remove(id);
        for (Integer subTaskId : epicTask.getSubTaskIds()) {
            subtasks.remove(subTaskId);
        }
    }

    @Override
    public void delSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubTaskIds().remove(Integer.valueOf(id));
                subtasks.remove(id);
                updateEpicStatus(epic);
            }
        }
    }


    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }
}

