package ru.yandex.javacource.aldukhov.schedule.manager;

public class Managers {
    private static TaskManager taskManager;
    private static HistoryManager historyManager;

    private Managers() {
    }

    public static HistoryManager getDefaultHistory() {
        if (historyManager == null) {
            historyManager = new InMemoryHistoryManager();
        }
        return historyManager;
    }

    public static TaskManager getDefault() {
        if (taskManager == null) {
            taskManager = new InMemoryTaskManager();
        }
        return taskManager;
    }
}