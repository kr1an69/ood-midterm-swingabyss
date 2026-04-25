package swingabyss.controller;

/**
 * Interface cho mô hình Command Pattern.
 * Đóng gói một hành động (như Attack, Skill) thành một đối tượng độc lập.
 */
public interface ICommand {
    /**
     * Phương thức thực thi nội dung lệnh.
     */
    void execute();
}
