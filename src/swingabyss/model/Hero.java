package swingabyss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho Nhân vật điều khiển bởi người chơi (Hero).
 */
public class Hero extends Entity {

    // Danh sách kỹ năng của Hero (Strategy Pattern)
    private List<ISkill> skills;

    public Hero(String name, Stats stats) {
        super(name, stats);
        this.skills = new ArrayList<>();
    }

    /**
     * Thêm kỹ năng mới cho nhân vật.
     */
    public void addSkill(ISkill skill) {
        this.skills.add(skill);
    }

    /**
     * Lấy danh sách kỹ năng hiện có.
     */
    public List<ISkill> getSkills() {
        return skills;
    }
}
