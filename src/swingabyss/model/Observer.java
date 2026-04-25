package swingabyss.model;

/**
 * Interface cho mô hình Observer Pattern.
 * Các thành phần giao diện (như GamePanel) sẽ implement interface này 
 * để được thông báo mỗi khi có thay đổi trạng thái (ví dụ: mất máu).
 */
public interface Observer {
    void onNotify(Entity entity);
}
