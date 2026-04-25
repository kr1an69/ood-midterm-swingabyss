package swingabyss.model;

import swingabyss.controller.AttackCommand;
import swingabyss.controller.DefendCommand;
import swingabyss.controller.HealCommand;
import swingabyss.controller.ICommand;

public class TestModel {
    public static void main(String[] args) {
        // Tạo Stats cho Hero và Monster
        Stats heroStats = new Stats(100, 15, 5, 10);
        Hero knight = new Hero("Knight", heroStats);

        Stats monsterStats = new Stats(50, 10, 2, 8);
        Monster goblin = new Monster("Goblin", monsterStats, 1);

        // Tạo một Observer ẩn danh (thay thế cho GamePanel)
        Observer uiObserver = new Observer() {
            @Override
            public void onNotify(Entity entity) {
                System.out.println("[UI_UPDATE] Entity: " + entity.getName() + " vừa cập nhật trạng thái!");
                System.out.println("            -> HP hiện tại: " + entity.getCurrentHp() + "/" + entity.getStats().getMaxHp());
                System.out.println("            -> Defense hiện tại: " + entity.getStats().getDefense());
            }
        };

        knight.addObserver(uiObserver);
        goblin.addObserver(uiObserver);

        System.out.println("=== BẮT ĐẦU TRẬN CHIẾN BẰNG COMMAND PATTERN ===");

        System.out.println("\n--- LƯỢT 1: GOBLIN ĐÁNH, KNIGHT THỦ ---");
        ICommand defendCmd = new DefendCommand(knight);
        defendCmd.execute(); // Knight phòng thủ, buff giáp từ 5 lên 10

        ICommand goblinAtk = new AttackCommand(goblin, knight);
        goblinAtk.execute(); // Goblin có 10 atk. Knight có 10 def -> Sát thương 0.
        
        System.out.println("\n--- LƯỢT 2: KNIGHT ĐÁNH, GOBLIN ĐÁNH LẠI ---");
        // Reset giáp của Knight sau khi qua lượt (thực tế TurnManager sẽ làm chuyện này)
        knight.getStats().setDefense(5);
        
        ICommand knightAtk = new AttackCommand(knight, goblin);
        knightAtk.execute(); // Knight 15 atk đánh Goblin 2 def -> Mất 13 máu.

        goblinAtk.execute(); // Goblin 10 atk đánh Knight 5 def -> Mất 5 máu.

        System.out.println("\n--- LƯỢT 3: KNIGHT DÙNG HEAL ---");
        ICommand knightHeal = new HealCommand(knight);
        knightHeal.execute(); // Lần 1
        knightHeal.execute(); // Lần 2
        knightHeal.execute(); // Lần 3
        knightHeal.execute(); // Lần 4 (Sẽ báo hết lượt Heal)
    }
}
