package ru.yandex.javacource.aldukhov.schedule.manager;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @Before
    public void setUp() throws Exception {
        tempFile = File.createTempFile("temp", ".txt");
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    public void testSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtask().isEmpty());
    }

    @Test
    public void testSaveAndLoadMultipleTasks() {
        Task task1 = new Task("Задача 1", "Описание задачи 1");
        Task task2 = new Task("Задача 2", "Описание задачи 2");
        Epic epic1 = new Epic("Эпик 1", "Описание эпика 1");
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", 3);

        manager.addNewTask(task1);
        manager.addNewTask(task2);
        manager.addNewEpic(epic1);
        manager.addNewSubtask(subtask1);
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(2, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(1, loadedManager.getSubtask().size());

        assertEquals(task1.getName(), loadedManager.taskById(task1.getId()).getName());
        assertEquals(task2.getName(), loadedManager.taskById(task2.getId()).getName());
        assertEquals(epic1.getName(), loadedManager.epicById(epic1.getId()).getName());
        assertEquals(subtask1.getName(), loadedManager.subtaskById(subtask1.getId()).getName());
    }
}
