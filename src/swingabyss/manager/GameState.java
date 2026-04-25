package swingabyss.manager;

/**
 * Tập hợp các trạng thái của cỗ máy FSM (Finite State Machine).
 */
public enum GameState {
    START_WAVE,     // Chuẩn bị đợt quái mới
    CHECK_TURN,     // Kiểm tra xem đến lượt ai dựa trên tốc độ
    HERO_ACTION,    // Lượt người chơi, từ giao diện cần đẩy xuống lệnh Action bằng pushCommand
    MONSTER_ACTION, // Máy tính tự điều khiển quái
    END_WAVE        // Phần xuất hiện khi quái chết hết, hiện bảng Chọn Phần thưởng (Reward)
}
