package ru.yandex.javacource.aldukhov.schedule.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.aldukhov.schedule.http.HttpTaskServer;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTasksTest {

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
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task task = new Task("Задача 1", "Описание задачи 1", Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromResponse = gson.fromJson(response.body(), new TypeToken<List<Task>>() {
        }.getType());
        assertNotNull(tasksFromResponse);
        assertEquals(1, tasksFromResponse.size());
        assertEquals("Задача 1", tasksFromResponse.get(0).getName());
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Задача 2", "Описание задачи 2", Duration.ofHours(2), LocalDateTime.now());
        int taskId = taskManager.addNewTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task taskFromResponse = gson.fromJson(response.body(), Task.class);
        assertNotNull(taskFromResponse);
        assertEquals("Задача 2", taskFromResponse.getName());
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Задача 3", "Описание задачи 3", Duration.ofHours(1), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.
                ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager);
        assertEquals(1, tasksFromManager.size());
        assertEquals("Задача 3", tasksFromManager.get(0).getName());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException, NotFoundException {
        Task task = new Task("Задача 4", "Описание задачи 4", Duration.ofHours(3), LocalDateTime.now());
        int taskId = taskManager.addNewTask(task);
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).
                POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updatedTask = taskManager.taskById(taskId);
        assertEquals("Задача 4", updatedTask.getName());
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException, NotFoundException {
        Task task = new Task("Задача 5", "Описание задачи 5", Duration.ofHours(4), LocalDateTime.now());
        int taskId = taskManager.addNewTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class, () -> taskManager.taskById(taskId));
    }
}
