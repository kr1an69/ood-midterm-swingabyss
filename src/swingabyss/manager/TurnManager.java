package swingabyss.manager;

import swingabyss.model.Entity;
import swingabyss.model.Hero;
import swingabyss.model.Monster;
import swingabyss.controller.ICommand;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Quản lý vòng lặp lượt đánh dựa trên FSM.
 * Trái tim điều phối luồng trò chơi giữa View/Panel và nhóm Model.
 */
public class TurnManager {
    private GameState currentState;
    private List<Hero> heroes;
    private List<Monster> monsters;
    
    // Hàng đợi Command từ lệnh điều khiển của người chơi trên UI
    private Queue<ICommand> commandQueue;
    
    public TurnManager() {
        // TODO: Khởi tạo danh sách heroes, monsters, queue và gán trạng thái ban đầu
    }
    
    /**
     * Chạy cỗ máy trạng thái (FSM). 
     * Nó sẽ đọc currentState hiện tại và quyết định làm việc tiếp theo.
     */
    public void processNextTurn() {
        // TODO: Viết logic switch-case theo currentState (như HERO_ACTION, MONSTER_ACTION, v.v.)
    }
    
    /**
     * Dùng để chuyển trạng thái FSM một cách an toàn.
     */
    public void changeState(GameState s) {
        this.currentState = s;
        // TODO: Có thể thêm logic setup cần thiết mỗi khi vào state mới
    }
    
    /**
     * Nhận lệnh Command (Ví dụ: sau khi click Attack ⚔). Đẩy vào hàng đợi và xử lý.
     */
    public void pushCommand(ICommand cmd) {
        // TODO: Thêm cmd vào commandQueue và tiến hành gọi processNextTurn()
    }
    
    // --- Các hàm Getter phục vụ UI (GamePanel, HUD) ---
    public GameState getCurrentState() {
        return currentState;
    }
    
    public Entity getCurrentActor() {
        // TODO: Lấy và trả về thực thể đang tới lượt
        return null;
    }
    
    public Entity getSelectedTarget() {
        // TODO: Lấy mục tiêu đang bị khóa
        return null;
    }
    
    public int getCurrentWave() {
        // TODO: Trả về đợt màn chơi hiện tại
        return 1;
    }
}
