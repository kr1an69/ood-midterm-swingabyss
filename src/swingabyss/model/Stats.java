package swingabyss.model;

/**
 * Lớp đại diện cho các chỉ số cơ bản của một thực thể (Entity).
 * Sử dụng Composition: Entity sẽ "có một" (has-a) Stats thay vì tự chứa các biến này.
 * Giúp dễ dàng quản lý, tính toán và mở rộng (ví dụ: buff/debuff) sau này.
 */
public class Stats {
    private int maxHp;
    private int attack;
    private int defense;
    private int speed;

    public Stats(int maxHp, int attack, int defense, int speed) {
        this.maxHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
    }

    // --- Getters & Setters ---
    
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getAttack() { return attack; }
    public void setAttack(int attack) { this.attack = attack; }

    public int getDefense() { return defense; }
    public void setDefense(int defense) { this.defense = defense; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    /**
     * Dùng cho Monster ở các Wave cao, nhân bản sức mạnh.
     * @param multiplier Hệ số nhân
     */
    public void scaleStats(float multiplier) {
        this.maxHp = (int) (this.maxHp * multiplier);
        this.attack = (int) (this.attack * multiplier);
        this.defense = (int) (this.defense * multiplier);
        // speed có thể giữ nguyên hoặc tăng nhẹ tùy design
    }
}
