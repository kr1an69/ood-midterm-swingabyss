package swingabyss.model;

/**
 * Interface định nghĩa các hành động chung cho mọi thực thể trong game.
 */
public interface IEntity {
    /**
     * Entity nhận sát thương.
     * @param amount Lượng sát thương nhận vào.
     */
    void takeDamage(int amount);

    /**
     * Hồi máu cho Entity.
     * @param amount Lượng máu được hồi.
     */
    void heal(int amount);

    /**
     * Kiểm tra trạng thái đã bị hạ gục hay chưa.
     * @return true nếu máu <= 0
     */
    boolean isDead();
}
