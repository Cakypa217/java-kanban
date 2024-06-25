import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subTaskIds;

    public Epic(String name, String description) {
        super(name, description);
        this.subTaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTaskIds() {
        return subTaskIds;
    }

    public void setSubTaskIds(ArrayList<Integer> subTaskIds) {
        this.subTaskIds = subTaskIds;
    }

    public void addSubtaskId(Integer subtaskId) {
        if (!subTaskIds.contains(subtaskId)) {
            subTaskIds.add(subtaskId);
        }
    }
}