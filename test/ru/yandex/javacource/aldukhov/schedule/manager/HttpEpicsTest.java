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

public class HttpEpicsTest {
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
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 1", "Описание эпика 1");
        taskManager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromResponse = gson.fromJson(response.body(), new TypeToken<List<Epic>>() {
        }.getType());
        assertNotNull(epicsFromResponse);
        assertEquals(1, epicsFromResponse.size());
        assertEquals("Эпик 1", epicsFromResponse.get(0).getName());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 2", "Описание эпика 2");
        int epicId = taskManager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic epicFromResponse = gson.fromJson(response.body(), Epic.class);
        assertNotNull(epicFromResponse);
        assertEquals("Эпик 2", epicFromResponse.getName());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 3", "Описание эпика 3");
        int epicId = taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача 1", "Описание подзадачи 1",
                epicId, Duration.ofHours(1), LocalDateTime.now());
        taskManager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
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
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик 4", "Описание эпика 4");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).
                POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = taskManager.getEpics();
        assertNotNull(epicsFromManager);
        assertEquals(1, epicsFromManager.size());
        assertEquals("Эпик 4", epicsFromManager.get(0).getName());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException, NotFoundException {
        Epic epic = new Epic("Эпик 5", "Описание эпика 5");
        int epicId = taskManager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertThrows(NotFoundException.class, () -> taskManager.epicById(epicId));
    }
}
