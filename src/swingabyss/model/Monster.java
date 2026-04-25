package swingabyss.model;

/**
 * Lớp đại diện cho Quái vật do máy AI điều khiển (Monster).
 */
public class Monster extends Entity {

    // Cấp độ của quái vật theo Wave (dùng để scale sức mạnh nếu cần)
    private int waveTier;

    public Monster(String name, Stats stats, int waveTier) {
        super(name, stats);
        this.waveTier = waveTier;
    }

    public int getWaveTier() {
        return waveTier;
    }
}
