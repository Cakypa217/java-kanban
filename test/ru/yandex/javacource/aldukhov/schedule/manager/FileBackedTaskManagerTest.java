package ru.yandex.javacource.aldukhov.schedule.manager;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.io.IOException;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

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
    public void testLoadFromNonExistentFile() {
        File nonExistentFile = new File("non_existent_file.txt");
        assertThrows(ManagerSaveException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
        }, "Загрузка из несуществующего файла должна вызывать ManagerSaveException");
    }

    @Test
    public void testSaveToReadOnlyFile() throws IOException {
        File file = new File("test_file.txt");
        file.createNewFile();

        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task = new Task("Задача", "Описание", Duration.ofHours(1), LocalDateTime.now());
        manager.addNewTask(task);

        file.setReadOnly();

        assertThrows(ManagerSaveException.class, () -> {
            manager.save();
        }, "Сохранение в файл только для чтения должно вызывать ManagerSaveException");

        file.delete();
    }

    @Test
    public void testSuccessfulSaveAndLoad() {
        assertDoesNotThrow(() -> {
            FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
            Task task = new Task("Test Task", "Description", Duration.ofHours(1), LocalDateTime.now());
            manager.addNewTask(task);
            manager.save();

            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
            assertEquals(1, loadedManager.getTasks().size());
        }, "Успешное сохранение и загрузка не должны вызывать исключений");
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
        Task task1 = new Task(1, "Задача 1", "Описание задачи 1", Status.NEW, Type.TASK);
        task1.setStartTime(LocalDateTime.now());
        task1.setDuration(Duration.ofHours(1));
        Task task2 = new Task(2, "Задача 2", "Описание задачи 2", Status.NEW, Type.TASK);
        task2.setStartTime(LocalDateTime.now().plusHours(2));
        task2.setDuration(Duration.ofHours(1));
        Epic epic1 = new Epic(3, "Эпик 1", "Описание эпика 1", Status.NEW, Type.EPIC);
        Subtask subtask1 = new Subtask(4, "Подзадача 1", "Описание подзадачи 1", Status.NEW,
                Type.SUBTASK, epic1.getId());
        subtask1.setStartTime(LocalDateTime.now().plusHours(4));
        subtask1.setDuration(Duration.ofHours(1));

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
    }
}
