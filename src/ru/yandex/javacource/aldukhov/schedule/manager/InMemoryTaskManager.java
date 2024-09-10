package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected int generatorId = 0;
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>((t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null) return 0;
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;
        return t1.getStartTime().compareTo(t2.getStartTime());
    });

    @Override
    public int addNewTask(Task task) {
        int id = ++generatorId;
        task.setId(id);
        checkTasksIntersection(task);
        tasks.put(id, task);
        prioritizedTasks.add(task);
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
        checkTasksIntersection(subtask);
        subtasks.put(id, subtask);
        prioritizedTasks.add(subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpic(epicId);
        return id;
    }

    @Override
    public void updateTask(Task task) {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        checkTasksIntersection(task);
        tasks.put(id, task);
        prioritizedTasks.remove(savedTask);
        prioritizedTasks.add(task);
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
        checkTasksIntersection(subtask);
        subtasks.put(id, subtask);
        prioritizedTasks.remove(saveSubtask);
        prioritizedTasks.add(subtask);
        updateEpic(epicId);
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
            updateEpic(epic.getId());
        }
        subtasks.clear();
    }

    @Override
    public Task taskById(int id) throws NotFoundException {
        Task task = tasks.get(id);
        historyManager.add(task);
        if (task == null) {
            throw new NotFoundException("Задачи с id " + id + " нет");
        }
        return task;
    }

    @Override
    public Epic epicById(int id) throws NotFoundException {
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Эпика с id " + id + " нет");
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public Subtask subtaskById(int id) throws NotFoundException {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Подзадачи с id " + id + " нет");
        }
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
                updateEpic(epic.getId());
            }
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
        updateEpic(epic.getId());
    }

    protected void updateEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            updateTimeFields(epic);
            updateEpicStatus(epic);
        }
    }

    public void updateTimeFields(Epic epic) {
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setDuration(Duration.ZERO);
            epic.setStartTime(null);
            return;
        }

        LocalDateTime earliestStart = null;
        LocalDateTime latestEnd = null;
        Duration totalDuration = Duration.ZERO;

        for (Integer subtaskId : epic.getSubTaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
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

        epic.setStartTime(earliestStart);
        epic.setDuration(totalDuration);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public void checkTasksIntersection(Task task) throws TaskValidationException {
        if (task.getStartTime() == null) {
            throw new TaskValidationException("Невозможно добавить задачу: отсутствует время начала");
        }
        for (Task existingTask : prioritizedTasks) {
            if (existingTask.getId() != task.getId() && tasksIntersect(task, existingTask)) {
                throw new TaskValidationException("Задача пересекается с id=" + existingTask.getId() +
                        " c " + existingTask.getStartTime() + " по " + existingTask.getEndTime());
            }
        }
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

