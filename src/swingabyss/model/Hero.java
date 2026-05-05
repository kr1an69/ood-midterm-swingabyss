package swingabyss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp đại diện cho Nhân vật điều khiển bởi người chơi (Hero).
 */
public class Hero extends Entity {

    // Danh sách kỹ năng của Hero (Strategy Pattern)
    private List<ISkill> skills;

    // Trạng thái kỹ năng hồi máu (Charge)
    private int healCharges = 5;
    private int maxHealCharges = 10;

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

    public int getHealCharges() {
        return healCharges;
    }

    public int getMaxHealCharges() {
        return maxHealCharges;
    }

    public void useHealCharge() {
        if (healCharges > 0) {
            healCharges--;
        }
    }

    public void addMaxHealCharges(int amount) {
        maxHealCharges += amount;
        healCharges += amount;
    }
}
