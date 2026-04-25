package swingabyss.model;

/**
 * Interface đại diện cho một Kỹ năng (Action) của nhân vật.
 * Đây là core của Strategy Pattern, cho phép nhân vật thay đổi hành vi tấn công/hồi máu
 * một cách linh hoạt mà không cần sửa code của Hero/Monster.
 */
public interface ISkill {
    /**
     * Tên kỹ năng để hiển thị lên UI
     */
    String getName();

    /**
     * Thực thi kỹ năng lên mục tiêu
     * @param user Người dùng kỹ năng
     * @param target Mục tiêu bị tác động
     */
    void execute(Entity user, Entity target);
}
