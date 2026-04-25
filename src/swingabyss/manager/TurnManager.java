package swingabyss.manager;

import swingabyss.model.Entity;
import swingabyss.model.Hero;
import swingabyss.model.Monster;
import swingabyss.controller.AttackCommand;
import swingabyss.controller.ICommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Quản lý vòng lặp lượt đánh dựa trên FSM.
 * Trái tim điều phối luồng trò chơi giữa View/Panel và nhóm Model.
 */
public class TurnManager {
    private GameState currentState;
    private List<Hero> heroes;
    private List<Monster> monsters;
    
    // Thuật toán lượt đánh (Turn Order)
    private List<Entity> turnOrder;
    private int currentActorIndex;
    private int currentWave;

    private Queue<ICommand> commandQueue;
    
    public TurnManager(List<Hero> heroes, List<Monster> monsters) {
        this.heroes = heroes != null ? heroes : new ArrayList<>();
        this.monsters = monsters != null ? monsters : new ArrayList<>();
        this.commandQueue = new LinkedList<>();
        this.turnOrder = new ArrayList<>();
        this.currentWave = 1;
        this.currentState = GameState.MAIN_MENU; // Bắt đầu ở Main Menu
    }
    
    /**
     * Chạy cỗ máy trạng thái (FSM). 
     */
    public void processNextTurn() {
        switch (currentState) {
            case MAIN_MENU:
                // Chờ UI gọi startGame()
                break;

            case START_WAVE:
                System.out.println("\n[FSM] Bắt đầu Wave " + currentWave);
                buildTurnOrder();
                changeState(GameState.CHECK_TURN);
                processNextTurn(); // Chạy ngay logic kiểm tra lượt
                break;

            case CHECK_TURN:
                // 1. Lọc điều kiện thắng thua
                if (getAliveHeroes().isEmpty()) {
                    changeState(GameState.GAME_OVER);
                    System.out.println("[FSM] Toàn bộ Hero đã chết. GAME OVER.");
                    return;
                }
                if (getAliveMonsters().isEmpty()) {
                    changeState(GameState.REWARD_PHASE);
                    System.out.println("[FSM] Quái đã chết hết. Chuyển sang chọn thưởng.");
                    return;
                }

                // 2. Chạy index lượt
                if (currentActorIndex >= turnOrder.size()) {
                    // Hết Round -> Sort lại từ đầu
                    buildTurnOrder();
                }

                Entity currentActor = turnOrder.get(currentActorIndex);
                
                // [GIẢI QUYẾT BÀI TOÁN XÁC CHẾT]: Nếu nhân vật này đã chết trước khi tới lượt -> bỏ qua
                if (currentActor.isDead()) {
                    currentActorIndex++;
                    processNextTurn();
                    return;
                }

                // Reset giáp phòng thủ (buff từ DefendCommand lượt trước của người này)
                currentActor.getStats().setDefense(currentActor.getStats().getDefense() % 10); // Logic tạm, thực tế cần biến gốc

                // Quyết định ai đánh
                System.out.println("[FSM] Tới lượt của: " + currentActor.getName());
                if (currentActor instanceof Hero) {
                    changeState(GameState.HERO_ACTION);
                    // Dừng ở đây chờ UI gọi pushCommand
                } else if (currentActor instanceof Monster) {
                    changeState(GameState.MONSTER_ACTION);
                    processNextTurn(); // Tự động đánh luôn
                }
                break;

            case HERO_ACTION:
                // Trạng thái này FSM đứng im chờ UI. Khi UI gọi pushCommand, hàm đó sẽ xử lý.
                break;

            case MONSTER_ACTION:
                // AI Quái vật tự động tấn công ngẫu nhiên
                Entity activeMonster = turnOrder.get(currentActorIndex);
                List<Hero> aliveHeroes = getAliveHeroes();
                if (!aliveHeroes.isEmpty()) {
                    Hero target = aliveHeroes.get(new Random().nextInt(aliveHeroes.size()));
                    ICommand attack = new AttackCommand(activeMonster, target);
                    attack.execute();
                }
                // Xong lượt
                currentActorIndex++;
                changeState(GameState.CHECK_TURN);
                processNextTurn();
                break;

            case REWARD_PHASE:
                // Đứng im chờ người chơi chọn thưởng. Sau khi chọn sẽ gọi nextWave()
                break;
                
            case GAME_OVER:
                // Kết thúc game
                break;
        }
    }

    /**
     * Dùng để chuyển trạng thái FSM một cách an toàn.
     */
    public void changeState(GameState s) {
        this.currentState = s;
    }
    
    /**
     * Nhận lệnh Command (Ví dụ: sau khi click Attack ⚔). Đẩy vào hàng đợi và xử lý.
     */
    public void pushCommand(ICommand cmd) {
        if (currentState == GameState.HERO_ACTION) {
            commandQueue.offer(cmd);
            ICommand activeCmd = commandQueue.poll();
            if (activeCmd != null) {
                activeCmd.execute();
                currentActorIndex++; // Hero đánh xong -> Next
                changeState(GameState.CHECK_TURN);
                processNextTurn();
            }
        }
    }

    // ==========================================
    // LOGIC HỖ TRỢ (UI & CƠ CHẾ)
    // ==========================================

    private void buildTurnOrder() {
        turnOrder.clear();
        turnOrder.addAll(getAliveHeroes());
        turnOrder.addAll(getAliveMonsters());
        
        // Cách 1: Sort theo Speed từ cao xuống thấp
        Collections.sort(turnOrder, new Comparator<Entity>() {
            @Override
            public int compare(Entity e1, Entity e2) {
                return Integer.compare(e2.getStats().getSpeed(), e1.getStats().getSpeed());
            }
        });
        currentActorIndex = 0;
    }

    /**
     * THUẬT TOÁN HONKAI STAR RAIL: Trả về danh sách n nhân vật đánh tiếp theo.
     * Có khả năng LỌC XÁC CHẾT (Bỏ qua những kẻ isDead() == true) và NHÌN TRƯỚC VÒNG TƯƠNG LAI.
     */
    public List<Entity> getUpcomingTurns(int count) {
        List<Entity> result = new ArrayList<>();
        if (turnOrder.isEmpty() || (getAliveHeroes().isEmpty() && getAliveMonsters().isEmpty())) {
            return result;
        }

        int peekIndex = currentActorIndex;
        
        // Quét cho đến khi đủ số lượng count
        while (result.size() < count) {
            if (peekIndex >= turnOrder.size()) {
                peekIndex = 0; // Vòng lại đầu mảng (mô phỏng vòng đánh tiếp theo)
            }
            
            Entity e = turnOrder.get(peekIndex);
            // Quan trọng: Chỉ đưa người sống vào UI
            if (!e.isDead()) {
                result.add(e);
            }
            peekIndex++;
            
            // Đề phòng trường hợp tất cả chết hết bị kẹt vòng lặp vô tận
            if (getAliveHeroes().isEmpty() && getAliveMonsters().isEmpty()) break;
        }
        return result;
    }

    private List<Hero> getAliveHeroes() {
        List<Hero> alive = new ArrayList<>();
        for (Hero h : heroes) {
            if (!h.isDead()) alive.add(h);
        }
        return alive;
    }

    private List<Monster> getAliveMonsters() {
        List<Monster> alive = new ArrayList<>();
        for (Monster m : monsters) {
            if (!m.isDead()) alive.add(m);
        }
        return alive;
    }

    public void startGame() {
        this.currentWave = 1;
        changeState(GameState.START_WAVE);
        processNextTurn();
    }

    public void nextWave() {
        this.currentWave++;
        changeState(GameState.START_WAVE);
        processNextTurn();
    }

    // --- Getters ---
    public GameState getCurrentState() { return currentState; }
    public int getCurrentWave() { return currentWave; }
}
