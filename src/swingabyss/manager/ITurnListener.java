package swingabyss.manager;

import swingabyss.model.Entity;

/**
 * Lắng nghe sự kiện chuyển lượt từ TurnManager.
 * Được dùng bởi UIPanel để cập nhật các chỉ số động khi thay đổi người chơi.
 */
public interface ITurnListener {
    void onTurnChange(Entity activeActor);
}
