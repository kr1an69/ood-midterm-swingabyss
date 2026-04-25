package swingabyss.controller;

import swingabyss.model.Entity;

/**
 * Lớp đại diện cho hành động "Phòng thủ".
 * Tăng tạm thời chỉ số giáp (defense) cho nhân vật trong lượt này.
 */
public class DefendCommand implements ICommand {
    private Entity defender;
    private int defenseBuffAmount = 5; // Có thể đưa vào config nếu cần

    public DefendCommand(Entity defender) {
        this.defender = defender;
    }

    @Override
    public void execute() {
        if (!defender.isDead()) {
            System.out.println("[CMD] " + defender.getName() + " vao the phong thu!");
            int currentDef = defender.getStats().getDefense();
            defender.getStats().setDefense(currentDef + defenseBuffAmount);
            // Lưu ý: Cần có cơ chế reset defense sau mỗi đợt hoặc lượt (sẽ xử lý ở TurnManager)
        }
    }
}
