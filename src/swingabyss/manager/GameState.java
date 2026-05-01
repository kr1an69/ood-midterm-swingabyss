package swingabyss.manager;

/**
 * Tập hợp các trạng thái của cỗ máy FSM (Finite State Machine).
 */
public enum GameState {
    MAIN_MENU,      // Màn hình chính
    START_WAVE,     // Chuẩn bị đợt quái mới
    CHECK_TURN,     // Kiểm tra xem đến lượt ai dựa trên tốc độ
    HERO_ACTION,    // Lượt người chơi, từ giao diện cần đẩy xuống lệnh Action bằng pushCommand
    SELECT_TARGET,  // Chờ người chơi nhấp chuột chọn mục tiêu tấn công
    MONSTER_ACTION, // Máy tính tự điều khiển quái
    REWARD_PHASE,   // Phần xuất hiện khi quái chết hết, hiện bảng Chọn Phần thưởng (Roguelite)
    GAME_OVER       // Tất cả Hero đã chết
}
