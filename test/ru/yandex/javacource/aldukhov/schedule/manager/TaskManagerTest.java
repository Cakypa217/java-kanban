package ru.yandex.javacource.aldukhov.schedule.manager;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import ru.yandex.javacource.aldukhov.schedule.task.Epic;
import ru.yandex.javacource.aldukhov.schedule.task.Status;
import ru.yandex.javacource.aldukhov.schedule.task.Subtask;
import ru.yandex.javacource.aldukhov.schedule.task.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

public class TaskManagerTest {
    InMemoryTaskManager manager = new InMemoryTaskManager();

    @Test //проверьте, что экземпляры класса Task и наследники равны друг другу, если равен их id;
    public void equalityOfTasksWithSameId() {
        Task task = new Task("Задача 1", "Описание 1");
        final int taskId = manager.addNewTask(task);
        final Task savedTask = manager.taskById(taskId);
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        final int epicId = manager.addNewEpic(epic);
        final Epic savedEpic = manager.epicById(epicId);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", 2);
        final int subTaskId = manager.addNewSubtask(subtask);
        final Subtask savedSubTask = manager.subtaskById(subTaskId);

        assertNotNull(task, "Задача не найдена.");
        assertEquals(task, savedTask);
        assertNotNull(epic, "Задача не найдена.");
        assertEquals(epic, savedEpic);
        assertNotNull(subtask, "Задача не найдена.");
        assertEquals(subtask, savedSubTask);
    }

    @Test //проверьте, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    public void addingAnEpicToYourself() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        epic.setId(2);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", 2);
        subtask.setId(2);
        Assertions.assertNull(manager.addNewSubtask(subtask));
    }

    @Test //проверьте, что объект Subtask нельзя сделать своим же эпиком;
    public void makeYourSubtaskAnEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        final int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", epicId);
        final int subTaskId = manager.addNewSubtask(subtask);
        epic.addSubtaskId(subTaskId);
        epic.setId(subTaskId);
        Assertions.assertNull(manager.addNewEpic(epic));
    }

    @Test //утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    public void shouldReturnSameInstanceOfTaskManager() {
        TaskManager first = Managers.getDefault();
        TaskManager second = Managers.getDefault();
        assertSame(first, second);
    }

    @Test //утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    public void shouldReturnSameInstanceOfHistoryManager() {
        HistoryManager first = Managers.getDefaultHistory();
        HistoryManager second = Managers.getDefaultHistory();
        assertSame(first, second);
    }

    @Test //проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
    public void InMemoryTaskManagerTest() {
        Task task = new Task("Задача 1", "Описание 1");
        final int taskId = manager.addNewTask(task);
        assertEquals(task, manager.taskById(taskId));
        assertEquals(taskId, task.getId());

        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        final int epicId = manager.addNewEpic(epic);
        assertEquals(epic, manager.epicById(epicId));
        assertEquals(epicId, epic.getId());

        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", 2);
        final int subtaskId = manager.addNewSubtask(subtask);
        assertEquals(subtask, manager.subtaskById(subtaskId));
        assertEquals(subtaskId, subtask.getId());
    }

    @Test //задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    public void givenIdGeneratedId() {
        Task task = new Task("Задача 1", "Описание 1");
        manager.addNewTask(task);
        final int taskId = task.getId();
        manager.updateTask(new Task("Задача 2", "Описание 2"));
        assertEquals(taskId, task.getId());
    }

    @Test //тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    public void constancyOfTheTask() {
        Task task = new Task("Задача 1", "Описание 1");
        manager.addNewTask(task);
        assertEquals("Задача 1", task.getName());
        assertEquals("Описание 1", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
    }

    @Test //task, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    public void savingTaskWhenChanging() {
        Task task = new Task("Задача 1", "Описание 1");
        manager.addNewTask(task);
        manager.taskById(1);
        Task task1 = manager.taskById(1);
        List<Task> tasks = manager.getHistory();
        Task task2 = tasks.get(tasks.size() - 1);
        manager.updateTask(new Task("Задача 2", "Описание 2"));
        Assertions.assertEquals(task1, task2);
    }

    @Test //epic, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    public void savingEpicWhenChanging() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.addNewEpic(epic);
        manager.epicById(1);
        Epic epic1 = manager.epicById(1);
        epic1.setId(2);
        List<Task> epics = manager.getHistory();
        Task epic2 = epics.get(0);
        manager.updateEpic(new Epic("Эпик 2", "Описание эпика 2"));
        Assertions.assertEquals(epic1, epic2);
    }

    @Test //subtask, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    public void savingSubTaskWhenChanging() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", 1);
        manager.addNewSubtask(subtask);
        manager.subtaskById(2);
        Subtask subtask1 = manager.subtaskById(2);
        subtask1.setId(3);
        List<Task> subtasks = manager.getHistory();
        Task subtask2 = subtasks.get(subtasks.size() - 1);
        manager.updateSubtask(new Subtask("Подзадача 2", "Описание подзадачи 2", 1));
        Assertions.assertEquals(subtask1, subtask2);
    }

    @Test //встроенный связный список версий, а также операции добавления и удаления работают корректно.
    public void testLinkedListOperations() {
        InMemoryHistoryManager historyManager = new InMemoryHistoryManager();

        Task task1 = new Task("Задача 1", "Описание задачи 1");
        task1.setId(1);
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        task2.setId(2);
        Task task3 = new Task("Задача 3", "Описание задачи 3");
        task3.setId(3);

        // Проверка добавления
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));

        // Проверка удаления из середины
        historyManager.remove(2);
        history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));

        // Проверка удаления с начала
        historyManager.remove(1);
        history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task3, history.get(0));

        // Проверка удаления с конца
        historyManager.remove(3);
        history = historyManager.getHistory();
        assertTrue(history.isEmpty());

        // Проверка добавления после удаления всех элементов
        historyManager.add(task2);
        history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test // Внутри эпиков не должно оставаться неактуальных id подзадач.
    public void testDeleteSubtaskRemovesIdFromEpic() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1", 1);
        int subtaskId = manager.addNewSubtask(subtask);
        manager.delSubtaskById(2);

        Epic updatedEpic = manager.epicById(epicId);
        assertFalse(updatedEpic.getSubTaskIds().contains(subtaskId));
    }

    @Test // Удаляемые подзадачи не должны хранить внутри себя старые id.
    public void testDeleteEpicRemovesAllSubtasks() {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = manager.addNewEpic(epic);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epicId);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epicId);
        int subtaskId1 = manager.addNewSubtask(subtask1);
        int subtaskId2 = manager.addNewSubtask(subtask2);

        manager.delEpicById(epicId);

        assertNull(manager.subtaskById(subtaskId1));
        assertNull(manager.subtaskById(subtaskId2));
    }

    @Test //Сеттеры влияют на данные внутри менеджера
    // Решение: убрать сеттеры, при добавлении задач добавлять их поля вручную, или же обновление производить только
    // через менеджер.
    public void testSetterChangeDoesAffectManagerWithoutUpdate() {
        Task task = new Task("Задача", "Описание");
        int taskId = manager.addNewTask(task);

        task.setName("Измененная задача");
        task.setDescription("Измененое описание");

        Task retrievedTask1 = manager.taskById(taskId);
        assertEquals("Измененная задача", retrievedTask1.getName());
    }
}
