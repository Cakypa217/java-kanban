package ru.yandex.javacource.aldukhov.schedule.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.aldukhov.schedule.manager.NotFoundException;
import ru.yandex.javacource.aldukhov.schedule.manager.TaskManager;
import ru.yandex.javacource.aldukhov.schedule.manager.TaskValidationException;
import ru.yandex.javacource.aldukhov.schedule.task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            if ("GET".equals(method) && path.equals("/tasks")) {
                getTask(exchange);
            }
            if ("GET".equals(method) && path.matches("/tasks/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                getTaskById(exchange, id);
            }
            if ("POST".equals(method) && path.equals("/tasks")) {
                Task task = gson.fromJson(body, Task.class);
                createTask(exchange, task);
            } else if ("POST".equals(method) && path.matches("/tasks/\\d+")) {
                Task task = gson.fromJson(body, Task.class);
                updateTask(exchange, task);
            }
            if ("DELETE".equals(method) && path.matches("/tasks/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf("/") + 1));
                deleteTask(exchange, id);
            }
        } catch (NotFoundException e) {
            sendNotFound(exchange, e.getMessage());
        }
    }

    public void getTask(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getTasks();
        String json = gson.toJson(tasks);
        sendText(exchange, json);
    }

    public void getTaskById(HttpExchange exchange, int id) throws IOException, NotFoundException {
        Task task = taskManager.taskById(id);
        String json = gson.toJson(task);
        sendText(exchange, json);
    }

    public void createTask(HttpExchange exchange, Task task) throws IOException {
        try {
            taskManager.checkTasksIntersection(task);
            String id = String.valueOf(taskManager.addNewTask(task));
            sendCode(exchange, "Задача успешно добавлена, id = " + id);
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    public void updateTask(HttpExchange exchange, Task task) throws IOException {
        try {
            taskManager.checkTasksIntersection(task);
            taskManager.updateTask(task);
            sendCode(exchange, "Задача успешно обновлена");
        } catch (TaskValidationException e) {
            sendHasInteractions(exchange, e.getMessage());
        }
    }

    public void deleteTask(HttpExchange exchange, int id) throws IOException, NotFoundException {
        taskManager.delTaskById(id);
        sendText(exchange, "Задача успешно удалена");
    }
}
