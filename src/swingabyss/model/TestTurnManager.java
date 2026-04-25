package swingabyss.model;

import swingabyss.controller.AttackCommand;
import swingabyss.controller.ICommand;
import swingabyss.manager.TurnManager;

import java.util.ArrayList;
import java.util.List;

public class TestTurnManager {
    public static void main(String[] args) {
        // KỊCH BẢN TEST: Đổi tốc độ để Hero C (Speed 20) và Hero B (Speed 15) đi trước Monster A (Speed 10).
        // Hero C sẽ dùng AttackCommand đánh lượng sát thương cực lớn (đủ giết chết Monster A)
        // Chúng ta sẽ quan sát UI Turn Order trước và sau khi Monster A chết.

        // 1. Tạo Entity
        Stats heroCStats = new Stats(100, 100, 5, 20); // Speed = 20, Atk = 100
        Hero heroC = new Hero("Hero C", heroCStats);

        Stats heroBStats = new Stats(100, 10, 5, 15);  // Speed = 15, Atk = 10
        Hero heroB = new Hero("Hero B", heroBStats);

        Stats monsterAStats = new Stats(50, 10, 0, 10); // Speed = 10, HP = 50
        Monster monsterA = new Monster("Monster A", monsterAStats, 1);

        // 2. Add Observer in log
        Observer logObserver = new Observer() {
            @Override
            public void onNotify(Entity entity) {
                if (entity.isDead()) {
                    System.out.println("  >>> [SYSTEM] " + entity.getName() + " ĐÃ BỊ TIÊU DIỆT! <<<");
                }
            }
        };
        heroC.addObserver(logObserver);
        heroB.addObserver(logObserver);
        monsterA.addObserver(logObserver);

        // 3. Khởi tạo TurnManager
        List<Hero> heroes = new ArrayList<>();
        heroes.add(heroC);
        heroes.add(heroB);
        List<Monster> monsters = new ArrayList<>();
        monsters.add(monsterA);

        TurnManager turnManager = new TurnManager(heroes, monsters);
        
        System.out.println("=== KỊCH BẢN TEST: HONKAI STAR RAIL TURN ORDER ===");
        
        // 4. Bắt đầu Game -> Chuyển sang START_WAVE -> Sort xong đứng ở HERO_ACTION (Vì Hero C speed 20)
        turnManager.startGame();
        
        // --- LƯỢT CỦA HERO C ---
        System.out.println("\n[UI UPDATE] Nhìn trước tương lai lúc Monster A chưa chết (Lấy 5 lượt):");
        printUpcomingTurns(turnManager, 5);

        System.out.println("\n[PLAYER INPUT] Hero C quyết định chém Monster A (100 damage)!");
        ICommand attackKill = new AttackCommand(heroC, monsterA);
        
        // Đẩy lệnh cho FSM xử lý
        turnManager.pushCommand(attackKill);
        
        // NGAY LẬP TỨC Monster A sẽ chết (vì 100 dmg > 50 HP). 
        // TurnManager tự tăng index, và vòng về CHECK_TURN. 
        // Tới phiên Hero B (speed 15). FSM dừng lại ở HERO_ACTION của Hero B.

        // --- LƯỢT CỦA HERO B ---
        System.out.println("\n[UI UPDATE] Nhìn trước tương lai lúc Monster A ĐÃ CHẾT (Lấy 5 lượt):");
        printUpcomingTurns(turnManager, 5);
        
        System.out.println("\n[PLAYER INPUT] Hero B chém không khí (skip)!");
        ICommand skipCmd = new ICommand() {
            @Override
            public void execute() { System.out.println("[CMD] Hero B skip turn."); }
        };
        turnManager.pushCommand(skipCmd);

        // Vòng lặp kết thúc vì Monster chết hết, FSM chuyển sang REWARD_PHASE.
    }

    private static void printUpcomingTurns(TurnManager tm, int count) {
        List<Entity> upcoming = tm.getUpcomingTurns(count);
        System.out.print("THANH UI TURN ORDER: [ ");
        for (Entity e : upcoming) {
            System.out.print(e.getName() + " -> ");
        }
        System.out.println("... ]");
    }
}
