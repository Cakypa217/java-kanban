package ru.yandex.javacource.aldukhov.schedule.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacource.aldukhov.schedule.manager.TaskManager;
import ru.yandex.javacource.aldukhov.schedule.task.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if ("GET".equals(method) && path.equals("/prioritized")) {
            getPrioritizedTasks(exchange);
        }
    }

    public void getPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> tasks = taskManager.getPrioritizedTasks();
        String json = gson.toJson(tasks);
        sendText(exchange, json);
    }
}
