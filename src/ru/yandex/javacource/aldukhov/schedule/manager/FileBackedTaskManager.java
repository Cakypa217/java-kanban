package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.io.*;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public int addNewTask(Task task) {
        int id = super.addNewTask(task);
        save();
        return id;
    }

    @Override
    public Integer addNewEpic(Epic epic) {
        int id = super.addNewEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer id = super.addNewSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void delTaskById(int id) {
        super.delTaskById(id);
        save();
    }

    @Override
    public void delEpicById(int id) {
        super.delEpicById(id);
        save();
    }

    @Override
    public void delSubtaskById(int id) {
        super.delSubtaskById(id);
        save();
    }

    @Override
    public void clearTask() {
        super.clearTask();
        save();
    }

    @Override
    public void clearEpic() {
        super.clearEpic();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    protected void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                writer.write(taskToString(task) + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(epicToString(epic) + "\n");
            }
            for (Subtask subtask : getSubtask()) {
                writer.write(subtaskToString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить задачи в файл.", e);
        }
    }

    private String taskToString(Task task) {
        return String.format("%d,%s,%s,%s,%s",
                task.getId(), Type.TASK, task.getName(), task.getStatus().toString(), task.getDescription());
    }

    private String epicToString(Epic epic) {
        return String.format("%d,%s,%s,%s,%s",
                epic.getId(), Type.EPIC, epic.getName(), epic.getStatus().toString(), epic.getDescription());
    }

    private String subtaskToString(Subtask subtask) {
        return String.format("%d,%s,%s,%s,%s,%d",
                subtask.getId(), Type.SUBTASK, subtask.getName(), subtask.getStatus().toString(),
                subtask.getDescription(), subtask.getEpicId());
    }

    private Task fromString(String value) {
        if (value == null) {
            return null;
        }
        String[] files = value.split(",");
        int id = Integer.parseInt(files[0]);
        Type type = Type.valueOf(files[1]);
        String name = files[2];
        Status status = Status.valueOf(files[3]);
        String description = files[4];


        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                int epicId = Integer.parseInt(files[5]);
                Subtask subtask = new Subtask(name, description, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    private void load() {
        if (file.length() == 0) {
            return;
        }
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (!line.startsWith("id")) {
                    Task task = fromString(line);
                    if (task != null) {
                        if (task instanceof Epic epic) {
                            super.addNewEpic(epic);
                        } else if (task instanceof Subtask subtask) {
                            super.addNewSubtask(subtask);
                        } else {
                            super.addNewTask(task);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось загрузить задачи из файла.", e);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }
}
