package swingabyss.controller;

import swingabyss.model.Entity;

/**
 * Lớp đại diện cho hành động "Hồi máu".
 * Chỉ hồi máu cho bản thân người dùng và bị giới hạn số lần sử dụng (charge).
 */
public class HealCommand implements ICommand {
    private Entity healer;
    private int healAmount = 30; // Lượng máu hồi mặc định
    
    // Giới hạn lượt dùng (charge)
    private int maxCharges = 3;
    private int currentCharges;

    public HealCommand(Entity healer) {
        this.healer = healer;
        this.currentCharges = maxCharges;
    }

    @Override
    public void execute() {
        if (!healer.isDead()) {
            if (currentCharges > 0) {
                System.out.println("[CMD] " + healer.getName() + " dung ky nang Hoi Mau!");
                healer.heal(healAmount);
                currentCharges--;
                System.out.println("      So lan Hoi Mau con lai: " + currentCharges + "/" + maxCharges);
            } else {
                System.out.println("[CMD] " + healer.getName() + " het luot Hoi Mau!");
            }
        }
    }

    /**
     * Sạc lại kỹ năng hồi máu (thường dùng sau khi qua Wave/Roguelite reward).
     */
    public void recharge() {
        this.currentCharges = maxCharges;
    }
    
    public int getCurrentCharges() {
        return currentCharges;
    }
}
