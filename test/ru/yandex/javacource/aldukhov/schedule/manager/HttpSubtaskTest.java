package ru.yandex.javacource.aldukhov.schedule.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacource.aldukhov.schedule.http.HttpTaskServer;
import ru.yandex.javacource.aldukhov.schedule.task.Epic;
import ru.yandex.javacource.aldukhov.schedule.task.Subtask;
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

public class HttpSubtaskTest {

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
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Subtask> subtasksFromResponse = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertNotNull(subtasksFromResponse);
        assertEquals(1, subtasksFromResponse.size());
        assertEquals("Подзадача 1", subtasksFromResponse.get(0).getName());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 2", "Описание эпика 2");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 2", "Описание подзадачи 2",
                epicId, Duration.ofHours(2), LocalDateTime.now());
        int subtaskId = taskManager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask subtaskFromResponse = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(subtaskFromResponse);
        assertEquals("Подзадача 2", subtaskFromResponse.getName());
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 3", "Описание эпика 3");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 3", "Описание подзадачи 3",
                epicId, Duration.ofHours(3), LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).
                POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = taskManager.getSubtask();
        assertNotNull(subtasksFromManager);
        assertEquals(1, subtasksFromManager.size());
        assertEquals("Подзадача 3", subtasksFromManager.get(0).getName());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException, NotFoundException {
        Epic epic = new Epic("Эпик 4", "Описание эпика 4");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 4", "Описание подзадачи 4",
                epicId, Duration.ofHours(4), LocalDateTime.now());
        int subtaskId = taskManager.addNewSubtask(subtask);
        subtask.setName("Обновленная подзадача 4");
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).
                POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask updatedSubtask = taskManager.subtaskById(subtaskId);
        assertEquals("Обновленная подзадача 4", updatedSubtask.getName());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 5", "Описание эпика 5");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 5", "Описание подзадачи 5",
                epicId, Duration.ofHours(5), LocalDateTime.now());
        int subtaskId = taskManager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertThrows(NotFoundException.class, () -> taskManager.subtaskById(subtaskId));
    }
}
