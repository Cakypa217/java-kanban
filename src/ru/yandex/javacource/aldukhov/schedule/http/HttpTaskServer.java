package ru.yandex.javacource.aldukhov.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacource.aldukhov.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacource.aldukhov.schedule.manager.TaskManager;
import ru.yandex.javacource.aldukhov.schedule.utils.DurationAdapter;
import ru.yandex.javacource.aldukhov.schedule.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final Gson gson;
    private final TaskManager taskManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        this.server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        initHandlers();
    }

    private void initHandlers() {
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    public void start() {
        server.start();
        System.out.println("HTTP-сервер запущен на " + PORT + " порту!");
    }

    public void stop() {
        server.stop(0);
        System.out.println("HTTP-сервер остановлен");
    }

    public static void main(String[] args) throws IOException {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        HttpTaskServer server = new HttpTaskServer(manager);
    }
}
