import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    int nextId = 1;
    HashMap<Integer, Task> simpleTasks = new HashMap<>();
    HashMap<Integer, Epic> epicTasks = new HashMap<>();
    HashMap<Integer, Subtask> subTasks = new HashMap<>();

    public void add(Task task) {
        task.setId(nextId++);
        if (task.getClass() == Task.class) {
            simpleTasks.put(task.getId(), task);
        } else if (task.getClass() == Epic.class) {
            epicTasks.put(task.getId(), (Epic) task);
        } else if (task.getClass() == Subtask.class) {
            Subtask subtask = (Subtask) task;
            subTasks.put(subtask.getId(), subtask);
            Epic epicTask = epicTasks.get(subtask.getEpicId());
            if (epicTask != null) {
                epicTask.addSubtaskId(subtask.getId());
            }
        }
    }

    public void update(int id, Task task) {
        task.setId(id);
        if (simpleTasks.containsKey(id)) {
            if (task.getClass() == Task.class) {
                simpleTasks.put(id, task);
            }
        } else if (epicTasks.containsKey(id)) {
            if (task.getClass() == Epic.class) {
                epicTasks.put(id, ((Epic) task));
                updateEpicStatus((Epic) task);
            }
        } else if (subTasks.containsKey(id)) {
                if (task.getClass() == Subtask.class) {
                    subTasks.put(id, (Subtask) task);
                    Epic epic = epicTasks.get(((Subtask) task).getEpicId());
                    if (epic != null) {
                        updateEpicStatus(epic);
                    }
                }
        }
    }

    private void updateEpicStatus(Epic epic) {
        boolean allDone = true;
        boolean allNew = true;
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }
        for (Integer subTaskId : epic.getSubTaskIds()) {
            Subtask subtask = subTasks.get(subTaskId);
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }
        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    public String printTask() {
        String result = "";
        for (Task task : simpleTasks.values()) {
            result += task.toString() + "\n";
        }
        return result;
    }

    public String printEpic() {
        String result = "";
        for (Epic epic : epicTasks.values()) {
            result += epic.toString() + "\n";
        }
        return result;
    }

    public String printSubTask() {
        String result = "";
        for (Subtask subtask : subTasks.values()) {
            result += subtask.toString() + "\n";
        }
        return result;
    }

    public List<Subtask> getTaskOfEpic(Epic epic) {
        List<Subtask> subTaskOfEpic = new ArrayList<>();
        for (int id : epic.getSubTaskIds()) {
            Subtask subtask = subTasks.get(id);
            if (subtask != null) {
                subTaskOfEpic.add(subtask);
            }
        }
        return subTaskOfEpic;
    }

    public void clearTask() {
        simpleTasks.clear();
    }

    public void clearEpic() {
        epicTasks.clear();
    }

    public void clearSubTask() {
        subTasks.clear();
    }

    public Task taskById(int id) {
        if (simpleTasks.containsKey(id)) {
            return simpleTasks.get(id);
        } else if (epicTasks.containsKey(id)) {
            return epicTasks.get(id);
        } else if (subTasks.containsKey(id)) {
            return subTasks.get(id);
        }
        return null;
    }

    public void delById(int id) {
        if (simpleTasks.containsKey(id)) {
            simpleTasks.remove(id);
        } else if (epicTasks.containsKey(id)) {
            Epic epic = epicTasks.remove(id);
            for (Integer subTaskId : epic.getSubTaskIds()) {
                subTasks.remove(subTaskId);
            }
        } else if (subTasks.containsKey(id)) {
            Subtask subtask = subTasks.remove(id);
            Epic epic = epicTasks.get(subtask.getId());
            epic.getSubTaskIds().remove(subtask.getId());
            updateEpicStatus(epic);
        }
    }
}