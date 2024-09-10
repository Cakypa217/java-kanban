package ru.yandex.javacource.aldukhov.schedule.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.aldukhov.schedule.http.HttpTaskServer;
import ru.yandex.javacource.aldukhov.schedule.task.Epic;
import ru.yandex.javacource.aldukhov.schedule.task.Subtask;
import ru.yandex.javacource.aldukhov.schedule.task.Task;
import ru.yandex.javacource.aldukhov.schedule.utils.DurationAdapter;
import ru.yandex.javacource.aldukhov.schedule.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpPrioritizedAndHistoryTest {

    private HttpTaskServer server;
    private TaskManager taskManager;
    private Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager();
        server = new HttpTaskServer(taskManager);
        server.start();
        gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @AfterEach
    void shutDown() {
        server.stop();
    }

    @Test
    public void testPrioritized() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Описание задачи 1",
                Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task1);
        Task task2 = new Task("Задача 2", "Описание задачи 2",
                Duration.ofHours(1), LocalDateTime.now().plusHours(2));
        taskManager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        String responseBody = response.body();
        List<Task> prioritizedTasks = gson.fromJson(responseBody, new TypeToken<List<Task>>() {
        }.getType());

        assertEquals(2, prioritizedTasks.size());
        assertEquals(task1.getId(), prioritizedTasks.get(0).getId());
        assertEquals(task2.getId(), prioritizedTasks.get(1).getId());
        assertEquals(task1.getName(), prioritizedTasks.get(0).getName());
        assertEquals(task2.getName(), prioritizedTasks.get(1).getName());
    }

    @Test
    public void testHistory() throws IOException, InterruptedException, NotFoundException {
        Task task1 = new Task("Задача 1", "Описание задачи 1", Duration.ofHours(1), LocalDateTime.now());
        Task task2 = new Task("Задача 1", "Описание задачи 1",
                Duration.ofHours(1), LocalDateTime.now().plusHours(2));
        int task1Id = taskManager.addNewTask(task1);
        int task2Id = taskManager.addNewTask(task2);
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now().plusHours(5));
        int subtaskId = taskManager.addNewSubtask(subtask);
        taskManager.taskById(task1Id);
        taskManager.taskById(task2Id);
        taskManager.epicById(epicId);
        taskManager.subtaskById(subtaskId);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> history = taskManager.getHistory();

        Assert.assertEquals(4, history.size());
        assertTrue(history.contains(task1));
        assertTrue(history.contains(task2));
        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));
    }
}
