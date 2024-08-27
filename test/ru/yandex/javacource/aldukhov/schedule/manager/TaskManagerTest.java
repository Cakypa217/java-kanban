package ru.yandex.javacource.aldukhov.schedule.manager;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import ru.yandex.javacource.aldukhov.schedule.task.Epic;
import ru.yandex.javacource.aldukhov.schedule.task.Status;
import ru.yandex.javacource.aldukhov.schedule.task.Subtask;
import ru.yandex.javacource.aldukhov.schedule.task.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    InMemoryTaskManager manager = new InMemoryTaskManager();
    protected T taskManager;

    @BeforeEach
    void setUp() {
        taskManager = createTaskManager();
    }

    abstract T createTaskManager();

    @Test
    public void testAddNewTask() {
        Task task = new Task("Задача 1", "Описание задачи 1", Duration.ofHours(1), LocalDateTime.now());
        int id = manager.addNewTask(task);
        assertNotEquals(-1, id);
        assertEquals(task, manager.taskById(id));
    }

    @Test
    public void testAddNewEpic() {
        Epic epic = new Epic("Эпик 1", "Описание");
        int id = manager.addNewEpic(epic);
        assertNotNull(id);
        assertEquals(epic, manager.epicById(id));
    }

    @Test
    public void testAddNewSubtask() {
        Epic epic = new Epic("Эпик 1", "Описание");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        int subtaskId = manager.addNewSubtask(subtask);
        assertNotNull(subtaskId);
        assertEquals(subtask, manager.subtaskById(subtaskId));
        assertTrue(manager.epicById(epicId).getSubTaskIds().contains(subtaskId));
    }

    @Test
    public void testUpdateEpicStatus() {
        Epic epic = new Epic("Эпик 1", "Описание");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2",
                epicId, Duration.ofHours(1), LocalDateTime.now().plusHours(2));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);

        assertEquals(Status.NEW, manager.epicById(epicId).getStatus());

        subtask1.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        manager.updateEpic(epic);
        assertEquals(Status.IN_PROGRESS, manager.epicById(epicId).getStatus());

        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);
        manager.updateEpic(epic);
        assertEquals(Status.IN_PROGRESS, manager.epicById(epicId).getStatus());

        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        manager.updateEpic(epic);
        assertEquals(Status.DONE, manager.epicById(epicId).getStatus());
    }

    @Test
    public void testGetTasks() {
        Task task1 = new Task("Задача 1", "Описание 1",
                Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task("Задача 2", "Описание 2",
                Duration.ofHours(3), LocalDateTime.now().plusDays(1));
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        List<Task> tasks = manager.getTasks();
        assertEquals(2, tasks.size());
        assertTrue(tasks.contains(task1));
        assertTrue(tasks.contains(task2));
    }

    @Test
    public void testGetEpics() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);
        List<Epic> epics = manager.getEpics();
        assertEquals(2, epics.size());
        assertTrue(epics.contains(epic1));
        assertTrue(epics.contains(epic2));
    }

    @Test
    public void testGetSubtasks() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2",
                epicId, Duration.ofHours(2), LocalDateTime.now().plusDays(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        List<Subtask> subtasks = manager.getSubtask();
        assertEquals(2, subtasks.size());
        assertTrue(subtasks.contains(subtask1));
        assertTrue(subtasks.contains(subtask2));
    }

    @Test
    public void testGetTaskOfEpic() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2",
                epicId, Duration.ofHours(2), LocalDateTime.now().plusDays(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        List<Subtask> subtasksOfEpic = manager.getTaskOfEpic(epic);
        assertEquals(2, subtasksOfEpic.size());
        assertTrue(subtasksOfEpic.contains(subtask1));
        assertTrue(subtasksOfEpic.contains(subtask2));
    }

    @Test
    public void testClearTask() {
        Task task1 = new Task("Задача 1", "Описание 1",
                Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task("Задача 2", "Описание 2",
                Duration.ofHours(3), LocalDateTime.now().plusDays(1));
        manager.addNewTask(task1);
        manager.addNewTask(task2);
        manager.clearTask();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    public void testClearEpic() {
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Epic epic2 = new Epic("Эпик 2", "Описание эпика 2");
        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);
        manager.clearEpic();
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    public void testClearSubtasks() {
        Epic epic = new Epic("Эпик", "Описание эпика");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание 2",
                epicId, Duration.ofHours(2), LocalDateTime.now().plusDays(1));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.clearSubtasks();
        assertTrue(manager.getSubtask().isEmpty());
        assertTrue(manager.getTaskOfEpic(epic).isEmpty());
    }

    @Test
    public void testTaskById() {
        Task task = new Task("Задача 1", "Описание 1", Duration.ofHours(2), LocalDateTime.now());
        int taskId = manager.addNewTask(task);
        assertEquals(task, manager.taskById(taskId));
    }

    @Test
    public void testEpicById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = manager.addNewEpic(epic);
        assertEquals(epic, manager.epicById(epicId));
    }

    @Test
    public void testSubtaskById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        int subtaskId = manager.addNewSubtask(subtask);
        assertEquals(subtask, manager.subtaskById(subtaskId));
    }

    @Test
    public void testDelTaskById() {
        Task task = new Task("Задача 1", "Описание 1", Duration.ofHours(2), LocalDateTime.now());
        int taskId = manager.addNewTask(task);
        manager.delTaskById(taskId);
        assertNull(manager.taskById(taskId));
    }

    @Test
    public void testDelEpicById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = manager.addNewEpic(epic);
        manager.delEpicById(epicId);
        assertNull(manager.epicById(epicId));
    }

    @Test
    public void testDelSubtaskById() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        int subtaskId = manager.addNewSubtask(subtask);
        manager.delSubtaskById(subtaskId);
        assertNull(manager.subtaskById(subtaskId));
    }

    @Test
    public void testGetHistory() {
        Task task = new Task("Задача 1", "Описание 1", Duration.ofHours(2), LocalDateTime.now());
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int taskId = manager.addNewTask(task);
        int epicId = manager.addNewEpic(epic);
        manager.taskById(taskId);
        manager.epicById(epicId);
        List<Task> history = manager.getHistory();

        assertEquals(3, history.size());
        assertTrue(history.contains(task));
        assertTrue(history.contains(epic));
    }
}
