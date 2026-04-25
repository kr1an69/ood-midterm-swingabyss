# Hướng Dẫn Chi Tiết & Giải Đáp Về Design Patterns (Swing into the Abyss)

File này sinh ra để trực tiếp trả lời 3 câu hỏi của bạn và giải thích tường tận các Design Patterns đang được áp dụng trong "bộ xương" kiến trúc giai đoạn 3.

---

## PHẦN 1: TỰ HỎI - TỰ ĐÁP (Q&A)

### 1. Tại sao Entity lại chứa 1 list Observer? Interface sao lại được dùng làm Type?
Trong Java (và lập trình Hướng Đối Tượng nói chung), **Interface sinh ra chính xác là để làm kiểu dữ liệu (Type)**. 

Khi bạn khai báo `List<Observer>`, không có nghĩa là bạn đang khởi tạo rỗng (`new Observer()`) - điều này Java cấm. Mà ý nghĩa của nó là: *"Hãy tạo một danh sách chứa BẤT KỲ OBJECT NÀO, CỦA BẤT KỲ CLASS NÀO, miễn là class đó có gắn mác `implements Observer`"*.

**Ứng dụng:**
Thằng `Entity` (Nhân vật/Quái) chả cần biết ai đang xem nó. Có thể là cái `GamePanel` (vẽ thanh máu), có thể là `AudioPlayer` (phát tiếng kêu "Á" khi mất máu), hoặc `Console` in ra log. 
Thay vì `Entity` phải code chặt: `GamePanel.giamMauUi(); Audio.hetLen();` (quá cứng nhắc), nó chỉ cần ném tất cả vào `List<Observer>`. Khi bị mất máu, nó duyệt cái danh sách đó và nói: *"Này các anh (Observer), tôi vừa thay đổi máu đấy, làm nhiệm vụ `onNotify()` của các anh đi!"* (Tính Đa hình - Polymorphism).

### 2. Ý nghĩa của việc "đóng gói hành động" (Object) và xài Queue? Nó khác gì Event Listener?
Đúng, khái niệm này (Command Pattern) có họ hàng với Event Listener (như kiểu `actionPerformed` của nút bấm), nhưng nó "tiến hóa" hơn.

**Tại sao phải đóng gói thành Object?**
Event Listener thường dính chết vào nút bấm trên màn hình (UI). Khi bạn bấm nút "Attack", nếu code gọi trực tiếp `Quái.truMau(10)`, máu quái trừ tức thì. Cực kỳ dở!
Thay vào đó, ấn nút "Attack" chỉ tạo ra một Object là `AttackCommand`. Gói Object này giống như **"một lá thư chỉ thị"**, bên trong thư ghi rõ: *Kẻ tấn công là Hero A, Mục tiêu là Monster B*. Lá thư này cầm ném đi đâu cũng được, UI không còn dính líu gì tới logic nữa.

**Tại sao phải vứt vào hàng đợi `Queue` mà không thực thi ngay?**
Trong game Turn-based, thời gian trôi theo Lượt, và việc vẽ giao diện (Animation) tốn thời gian.
1. Nếu bạn bấm Attack, nhân vật nhảy lên chém mất 2 giây (Animation), bạn không thể lỡ tay bấm liên tục 10 lần và máu quái tụt về 0 trong khi hoạt ảnh chém chưa xong. 
2. Trật tự tốc độ (Speed): Có khi bạn (Hero) ra lệnh chém con Rồng, nhưng do Rồng có Speed cao hơn nên Rồng phải được chém trước. 
-> Giải pháp: Bỏ tất cả các "lá thư chỉ thị" đó vào `Queue` (hàng xếp hàng). `TurnManager` sẽ chậm rãi bốc từng lá thư ra ở trạng thái (State) `HERO_ACTION`. Chờ lệnh thực thi xong, chạy xong animation, mới đi xử lý lá thư tiếp theo. Nó chống việc game bị "kẹt mạng", giật giật hay sai thứ tự.

### 3. `pushCommand` nghĩa là sao?
Trong khoa học máy tính, hàng đợi Queue có thao tác đưa một phần tử vào đuôi hàng gọi là `Push` (hoặc `Enqueue`), và lấy phần tử ở đầu hàng gọi là `Pop` (hay `Dequeue`).

Hàm `pushCommand(ICommand cmd)` trong `TurnManager` đơn giản dịch ra là: **"Gửi chỉ thị này vào trong hộp thư của hệ thống."**
Khi người chơi ấn giao diện UI, UI gọi `turnManager.pushCommand(new AttackCommand(...))`. Hệ thống ngầm hiểu là lệnh của bạn đã được tiếp nhận và để vào hàng đợi chờ xử lý dần.

---

## PHẦN 2: TÌM HIỂU VỀ CÁC PATTERNS ĐÃ ÁP DỤNG TRONG GIAI ĐOẠN 3

### A. Observer Pattern (Mẫu Quan Sát)
* Cấu trúc liên quan: `Observer`, `Entity`
* **What (Là gì?):** Một Pattern dùng để tạo mối quan hệ 1-N (một - nhiều). Khi 1 đối tượng thay đổi trạng thái, nó tự động thông báo cho tất cả những đối tượng đang "đăng ký theo dõi" nó.
* **Why (Tại sao?):** Tách rời (Decoupling) View và Model. Ở GĐ 1, `GamePanel` vẽ thanh Máu hoàn toàn tách khỏi logic. Giờ máu do `Entity` giữ. Rõ ràng View không nên liên tục chọc vào Model 60 lần/giây để hỏi "mất máu chưa?", như vậy rất chậm. Thay vào đó, View cứ nằm im, hễ Model (Entity) thay đổi, Model sẽ giật còi (Notify) báo Views tự cập nhật. 
* **When (Khi nào xài?):** Khi bạn có hai mảng tách rời lớn (như UI và Data), hoặc khi một sự kiện xảy ra có vô số nơi cần bắt tín hiệu cập nhật mà bạn không muốn code "if/else" rườm rà.
* **How (Dùng thế nào?):** Interface `Observer` (có `onNotify()`), và interface Subject (`Entity`) chứa `List<Observer>`. View thì implements `Observer`, Add chính View đó vào List của Subject.

### B. Command Pattern (Mẫu Lệnh)
* Cấu trúc liên quan: `ICommand`, `AttackCommand`, `TurnManager.commandQueue`
* **What (Là gì?):** Đóng gói yêu cầu xử lý thành một Object thay vì một func call thông thường. 
* **Why (Tại sao?):** Trả lời Câu hỏi số 2. Cho phép hoãn lệnh (lưu vào Queue), lưu lại dấu vết (để làm chức năng Undo/Redo - VD hồi lại chiêu), và cho phép gán lệnh vào nút bấm ở ngoài giao diện UI mà UI không cần biết lệnh đó là gì. UI chỉ biết "tôi cầm object Command và ném nó cho hệ thống".
* **When (Khi nào xài?):** Game có UI Input phức tạp, game cần cơ chế Replay/Undo, hoặc Game Turn-Based nơi hành động rơi vào hàng đợi.
* **How (Dùng thế nào?):** Có `ICommand` mang phương thức `execute()`. Các class con như `AttackCommand` gói gọn tất cả dữ liệu cần thiết (actor, target, damage) vào constructor của nó. Hệ thống chỉ việc gọi `.execute()`.

### C. State Machine / FSM (Cỗ Máy Trạng Thái)
* Cấu trúc liên quan: `TurnManager`, `GameState`
* **What (Là gì?):** Theo dõi luồng hoạt động mà ở đó hệ thống CHỈ ĐƯỢC PHÉP nằm ở 1 trạng thái nhất định tại 1 thời điểm.
* **Why (Tại sao?):** Không có pattern này, vòng lặp game của bạn sẽ là "một đống lẩu Thập Cẩm": một cục `if (nguoichoiDanh) {...} else if (quaiDanh) {...} else if (chuyenMan) {...}` rất khó maintain. FSM giúp cô lập logic của từng pha độc lập.
* **When (Khi nào xài?):** Cực kỳ phổ biến để làm AI (ngủ -> đi dạo -> đuổi chém player -> bỏ ngang về), các Game Manager (Menu -> Playing -> GameOver), hoặc luồng Lượt(Turn) phức tạp.
* **How (Dùng thế nào?):** Sinh 1 Enum chứa trạng thái (VD `GameState`), tạo biến `currentState`. Dùng `switch-case` điều khiển hành vi thay đổi theo State. Viết 1 hàm `changeState()` để đảm bảo lúc nhảy trạng thái được trơn tru.

---

## PHẦN 3: GIẢI ĐÁP CÁC THẮC MẮC CHUYÊN SÂU (UPDATE)

### 4. Về câu "view không nên liên tục chọc vào model 60 lần/giây để hỏi", cụ thể code nó như thế nào?
Đúng vậy, thay vì **View đi check (hỏi) Model**, thì **Model tự thông báo** cho View khi có thay đổi.

* **Nếu không dùng Observer (View chọc vào Model 60 lần/giây - Polling):**
Bạn sẽ phải đưa logic kiểm tra thiết lập vào vòng lặp update/vẽ cập nhật liên tục (ví dụ chạy trong Timer). Code sẽ kiểu:
```java
// Việc này chạy vô hạn 60 lần 1 giây trong GamePanel
public void paintComponent(Graphics g) {
    if (hero.getHp() != hero.lastKnownHp) { // View chủ động "chọc" vào Model lấy data xem HP có đổi không
        hero.lastKnownHp = hero.getHp();
        // Cập nhật lại thanh máu...
    }
    // ...
}
```
Làm thế này thì View phải ôm đồm đi hỏi liên tục. Nếu có 100 con quái, mỗi miligiây View phải gọi 100 lần `getHp()`, cực kỳ lãng phí tài nguyên máy tính vì phần lớn thời gian máu của các nhân vật chả thay đổi gì cả (đâu phải lúc nào cũng bị đánh liên tục).

* **Nếu dùng Observer Pattern (Model chủ động báo - Event Driven):**
View (GamePanel) cứ đứng yên tâm vẽ nhàn nhã cái gì nó đang nhớ. Máu quái lúc nào mất thì thằng Entity tự "cầm cái loa" (duyệt array list Observer) hét lên:
```java
// Trong class Entity (Model):
public void takeDamage(int dmg) {
    this.hp -= dmg;
    notifyObservers(); // Chủ động gọi: "Anh em ơi máu tôi đổi rồi, tự tính toán update đi!"
}

// Trong GamePanel (View):
@Override
public void onNotify(Entity e) { // Hàm này của Observer
    repaint(); // View nằm im nghe tiếng loa báo phát là gọi hàm vẽ lại hệ thống ngay lập tức
}
```
Như vậy, hệ thống tiết kiệm được cực kỳ nhiều hiệu năng, và cũng tách biệt ai lo việc người nấy rõ rệt. Chỉ khi nào "bị đánh", hệ thống mới kích hoặt cập nhật UI (Chính vì "hét" lên nhưng lại bằng dạng list Observer, nên thằng Model nó chả quan tâm thằng View dùng cái loa đó để làm gì, vẽ máu rung bần bật, văng số ra hay gì kệ View).

### 5. Tại sao Command Pattern lại dùng Queue mà chức năng Undo/Redo lại cần Stack?
Bạn thắc mắc nội dung này cực kỳ chuẩn và tinh ý! Cần hiểu rõ **bản thân Object lưu lệnh ra sao**, Command Pattern cho phép đóng gói lệnh thành gói hàng rờ chạm được, nhưng **lưu gói hàng đó vào đâu (Queue hay Stack)** là do tính năng ta cần dùng chúng lúc sau đó như thế nào.

* **Dùng Queue (Hàng đợi - Vào trước ra trước):** Vì trong Game Turn-based của dự án này, các nhân vật ra chiêu là một chuỗi hành động theo thứ tự (ai speed cao đánh trước chờ tới lượt). Khi ấn nút, hoặc khi xếp lịch cho Quái, các lệnh Command được ném vào Queue. TurnManager lôi lệnh ra theo thứ tự `1 -> 2 -> 3` để các diễn hoạt ảnh chạy lần lượt. Đây gọi là **Delayed Execution** (Trì hoãn thực thi). Ném vào đợi đúng nhịp thì bung ra. Hợp lý với Game State Machine này.
* **Dùng Stack (Ngăn xếp - Vào sau ra trước):** Đúng như bạn nói, **nếu muốn làm Undo/Redo thao tác (Ctrl+Z)** trong Game hoặc Editor nói chung, phần lịch sử ta PHẢI nhét các Command đã thực thi thành công vào một `Stack<ICommand> history`. Khi bạn ấn Undo, hệ thống sẽ chọc lấy lớp vỏ thùng mì mới cất nhất đẩy ra bằng hàm `pop`, và gọi lệnh `command.undo()` để đảo ngược. 

Vậy nên bản thân object `Command` là linh hoạt. Muốn nó xếp hàng chờ đánh nhau thì bỏ vô Queue. Thiết kế History để Undo thì vất vào Stack. Ở game SwingIntoTheAbyss giai đoạn 3, ta dùng Queue vì mục đích chính là quản lý lượt theo nhịp và chờ thời gian Animation xong mới chạy lệnh tiếp. 

### 6. Tại sao State Machine (FSM) dùng Enum rồi vẫn có `switch(state)` (hay `if else`), vậy khác gì code bình thường?
Đây là một "Mind-blow" của rất nhiều Coder mới học khi nhìn vào FSM. Sự khác biệt không phải là việc biến mất keyword `if else`, mà nằm ở **SỰ CÔ LẬP TRẠNG THÁI VÀ BẢO HIỂM LỖI CHỒNG CHÉO**.

**Trường hợp 1: Không có FSM (Cầm 1 đống biến flag boolean điều kiện)**
Nếu không xài Enum State gom cụm, logic controller của bạn sẽ kiểm tra và phụ thuộc vào hàng tá cờ (flags) boolean mỗi khung hình:
```java
if (isHeroTurn && !isMonsterTurn && !isGameOver && !isChoosingReward) {
    // Xử lý lượt hero
} else if (!isHeroTurn && isMonsterTurn && !isGameOver) {
    // Xử lý lượt monster đánh
} else if (isGameOver && !isHeroTurn) { ... }
```
Cách này thì thật sự... dễ rối não! Quá nhiều chữ! Mà khi làm Game vài ba Feature thì bảo đảm bạn dễ dàng quên tắt cái `isChoosingReward = false` khi reset game, dẫn tới bug dính chung 2 trạng thái chạy chồng lên nhau cùng lúc (Nhập nhằng giữa màn chơi và màn Over).

**Trường hợp 2: Có State Machine (FSM)**
Khi quản lý bằng **MỘT VÀ CHỈ MỘT BỘ NHỚ BIẾN** `GameState currentState` duy nhất, đoạn xử lý sẽ như này:
```java
public void processTurn() {
    // Vẫn là cấu trúc điều kiện thôi, nhưng nó trong sáng!
    switch(currentState) { 
        case HERO_ACTION:
            handleHeroTurn();
            break;
        case MONSTER_ACTION:
            handleMonsterTurn();
            break;
        case END_WAVE:
            handleRewards();
            break;
    }
}
```
**Sự khác biệt cực lớn nằm ở chỗ:**
1. **Tuyệt đối an toàn (Mutually Exclusive):** Tại một phân cảnh thời gian bất kỳ `currentState` CHỈ CÓ THỂ NHẬN 1 GIÁ TRỊ DUY NHẤT DO ENUM ĐÓ QUY ĐỊNH. Bạn không bao giờ có thể "Vừa ở trạng thái `HERO_ACTION` vừa ở `END_WAVE`" được vì 1 lúc biến chỉ chứa chữ đó 1 lần. Tránh hoàn toàn lỗi chồng trạng thái logic game.
2. **Kiểm soát quá trình Chuyển Giao (Transitions):** Nhờ việc gói code, bạn có thể lập trình luật riêng: "Chỉ khi trạng thái đang là `CHECK_TURN` thì mới được nhảy tiếp tới `HERO_ACTION`". Code if-else thông thường rất dễ văng linh tinh không luồng. Dùng FSM có hàm set `changeState(GameState newState)` ép buộc việc chuyển Phase theo đúng thiết kế quy trình, gỡ rối lúc Bug rất nhanh.
