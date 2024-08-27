package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int generatorId = 0;
    protected HistoryManager historyManager = Managers.getDefaultHistory();
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    protected Set<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null) return 0;
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    @Override
    public int addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        if (task.getStartTime() != null && !checkTasksIntersection(task)) {
            tasks.put(id, task);
            prioritizedTasks.add(task);
            return id;
        }
        return -1;
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
        if (subtask.getStartTime() != null && !checkTasksIntersection(subtask)) {
            subtasks.put(id, subtask);
            prioritizedTasks.add(subtask);
            epic.addSubtaskId(subtask.getId());
            updateEpicStatus(epic);
            return id;
        }
        return null;
    }

    @Override
    public void updateTask(Task task) {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        if (task.getStartTime() != null && !checkTasksIntersection(task)) {
            tasks.put(id, task);
            prioritizedTasks.remove(savedTask);
            prioritizedTasks.add(task);
        }
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
        updateEpicStatus(epic);
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
        if (subtask.getStartTime() != null && !checkTasksIntersection(subtask)) {
            subtasks.put(id, subtask);
            prioritizedTasks.remove(saveSubtask);
            prioritizedTasks.add(subtask);
            updateEpicStatus(epic);
        }
    }

    private void updateEpicStatus(Epic epic) {
        List<Status> subtaskStatuses = epic.getSubTaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .map(Task::getStatus)
                .toList();

        if (subtaskStatuses.isEmpty()) {
            epic.setStatus(Status.NEW);
        } else if (subtaskStatuses.stream().allMatch(status -> status == Status.NEW)) {
            epic.setStatus(Status.NEW);
        } else if (subtaskStatuses.stream().allMatch(status -> status == Status.DONE)) {
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
        return epic.getSubTaskIds().stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void clearTask() {
        tasks.clear();
        prioritizedTasks.removeIf(task -> task.getType() == Type.TASK);
    }

    @Override
    public void clearEpic() {
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        prioritizedTasks.removeIf(task -> task.getType() == Type.SUBTASK);
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
        Task task = tasks.remove(id);
        if (task != null) {
            prioritizedTasks.remove(task);
        }
    }

    @Override
    public void delEpicById(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        Epic epicTask = epics.remove(id);
        if (epicTask != null) {
            epicTask.getSubTaskIds().forEach(subtaskId -> {
                Subtask removedSubtask = subtasks.remove(subtaskId);
                if (removedSubtask != null) {
                    prioritizedTasks.remove(removedSubtask);
                }
            });
        }
    }

    @Override
    public void delSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
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

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean checkTasksIntersection(Task newTask) {
        return prioritizedTasks.stream().anyMatch(task -> tasksIntersect(newTask, task));
    }

    private boolean tasksIntersect(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null
                || task1.getEndTime() == null || task2.getEndTime() == null) {
            return false;
        }
        return !(task1.getEndTime().isBefore(task2.getStartTime())
                || task1.getStartTime().isAfter(task2.getEndTime()));
    }
}

