package swingabyss.model;

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
                System.out.println("[UI_UPDATE] Entity: " + entity.getName() + "Vua cap nhat trang thai!");
                System.out.println(
                        "            -> HP hien tai: " + entity.getCurrentHp() + "/" + entity.getStats().getMaxHp());
            }
        };

        // Đăng ký theo dõi
        knight.addObserver(uiObserver);
        goblin.addObserver(uiObserver);

        System.out.println("=== Start Game ===");

        System.out.println("1. Goblin can Knight (Damage = 15)");
        // Knight nhận 15 sát thương, nhưng có 5 defense -> mất 10 máu
        knight.takeDamage(15);

        System.out.println("\n2. Knight chem Goblin (Damage = 20)");
        // Goblin nhận 20 sát thương, có 2 defense -> mất 18 máu
        goblin.takeDamage(20);

        System.out.println("\n3. Knight dung binh mau (Hoi 5 mau)");
        knight.heal(5);
    }
}
