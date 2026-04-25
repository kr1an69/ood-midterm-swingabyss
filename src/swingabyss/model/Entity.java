package swingabyss.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp trừu tượng đại diện cho một thực thể (Nhân vật hoặc Quái vật).
 * Đóng vai trò Subject trong Observer Pattern.
 */
public abstract class Entity implements IEntity {
    protected String name;
    protected int currentHp;
    
    // Sử dụng Composition: Đưa toàn bộ chỉ số tĩnh vào class Stats
    protected Stats stats;
    
    // Danh sách các đối tượng quan sát sự thay đổi của thực thể này
    protected List<Observer> observers;
    
    public Entity(String name, Stats stats) {
        this.name = name;
        this.stats = stats;
        this.currentHp = stats.getMaxHp(); // Khởi đầu bằng full máu
        this.observers = new ArrayList<>();
    }
    
    /**
     * Đăng ký một Observer vào lưới theo dõi.
     */
    public void addObserver(Observer o) {
        if (!this.observers.contains(o)) {
            this.observers.add(o);
        }
    }
    
    /**
     * Gỡ bỏ một Observer.
     */
    public void removeObserver(Observer o) {
        this.observers.remove(o);
    }
    
    /**
     * Thông báo cho tất cả các Observer khi có sự thay đổi.
     */
    protected void notifyObservers() {
        for (Observer o : observers) {
            o.onNotify(this);
        }
    }

    @Override
    public void takeDamage(int amount) {
        // Trừ đi giáp bảo vệ (defense), nhưng đảm bảo sát thương tối thiểu là 0
        int actualDamage = Math.max(0, amount - stats.getDefense());
        this.currentHp -= actualDamage;
        if (this.currentHp < 0) {
            this.currentHp = 0;
        }
        // Gọi hàm cập nhật giao diện
        notifyObservers();
    }

    @Override
    public void heal(int amount) {
        if (!isDead()) {
            this.currentHp += amount;
            if (this.currentHp > stats.getMaxHp()) {
                this.currentHp = stats.getMaxHp();
            }
            notifyObservers();
        }
    }

    @Override
    public boolean isDead() {
        return this.currentHp <= 0;
    }
    
    // --- Getters ---
    public String getName() {
        return name;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public Stats getStats() {
        return stats;
    }
}
