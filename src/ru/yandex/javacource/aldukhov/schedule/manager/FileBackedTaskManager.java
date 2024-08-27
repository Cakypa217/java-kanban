package ru.yandex.javacource.aldukhov.schedule.manager;

import ru.yandex.javacource.aldukhov.schedule.task.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String HEADER = "id,type,name,status,description,epic";
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
            writer.write(HEADER);
            writer.newLine();
            for (Task task : getTasks()) {
                writer.write(toString(task));
                writer.newLine();
            }
            for (Epic epic : getEpics()) {
                writer.write(toString(epic));
                writer.newLine();
            }
            for (Subtask subtask : getSubtask()) {
                writer.write(toString(subtask));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить задачи в файл.", e);
        }
    }

    private String toString(Task task) {
        return task.getId() + "," + task.getType() + "," + task.getName() + "," + task.getStatus() + ","
                + task.getDescription() + "," + (task.getType().equals(Type.SUBTASK)
                ? ((Subtask) task).getEpicId() : "");
    }

    private static Task fromString(String value) {
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
                return new Task(id, name, description, status, Type.TASK);
            case EPIC:
                return new Epic(id, name, description, status, Type.EPIC);
            case SUBTASK:
                int epicId = Integer.parseInt(files[5]);
                return new Subtask(id, name, description, status, Type.SUBTASK, epicId);

            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    protected void addAnyTask(Task task) {
        final int id = task.getId();
        switch (task.getType()) {
            case TASK:
                tasks.put(id, task);
                break;
            case SUBTASK:
                subtasks.put(id, (Subtask) task);
                break;
            case EPIC:
                epics.put(id, (Epic) task);
                break;
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        final FileBackedTaskManager taskManager = new FileBackedTaskManager(file);
        try {
            final String csv = Files.readString(file.toPath());
            final String[] lines = csv.split(System.lineSeparator());
            int generatorId = 0;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    break;
                }
                final Task task = fromString(line);
                final int id = task.getId();
                if (id > generatorId) {
                    generatorId = id;
                }
                taskManager.addAnyTask(task);
            }
            for (Map.Entry<Integer, Subtask> e : taskManager.subtasks.entrySet()) {
                final Subtask subtask = e.getValue();
                final Epic epic = taskManager.epics.get(subtask.getEpicId());
                epic.addSubtaskId(subtask.getId());
            }
            taskManager.generatorId = generatorId;
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось загрузить задачи из файла: " + file.getName(), e);
        }
        return taskManager;
    }
}
