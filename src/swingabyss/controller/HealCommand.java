package swingabyss.controller;

import swingabyss.model.Hero;

/**
 * Lớp đại diện cho hành động "Hồi máu".
 * Chỉ hồi máu cho bản thân người dùng và bị giới hạn số lần sử dụng (charge).
 */
public class HealCommand implements ICommand {
    private Hero healer;
    private int healAmount = 30; // Lượng máu hồi mặc định

    public HealCommand(Hero healer) {
        this.healer = healer;
    }

    @Override
    public void execute() {
        if (!healer.isDead()) {
            if (healer.getHealCharges() > 0) {
                System.out.println("[CMD] " + healer.getName() + " dung ky nang Hoi Mau!");
                healer.heal(healAmount);
                healer.useHealCharge();
                System.out.println("      So lan Hoi Mau con lai: " + healer.getHealCharges() + "/" + healer.getMaxHealCharges());
            } else {
                System.out.println("[CMD] " + healer.getName() + " het luot Hoi Mau!");
            }
        }
    }
}
