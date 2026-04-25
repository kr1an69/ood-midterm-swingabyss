# 📂 MASTER CONTEXT FILE: SWING INTO THE ABYSS
**Ngày cập nhật:** 25/04/2026 (Lần 3 — sau khi hoàn thành Giai đoạn 3: Core Logic)
**Sinh viên:** Trần Tuấn Anh – 24130016 – DH24DTD
**Repository (GitHub):** https://github.com/kr1an69/ood-midterm-swingabyss
**Mục đích file này:** Context Preservation cho mọi AI Agent tham gia dự án. Đọc **toàn bộ** file này trước khi làm bất cứ điều gì. Mọi quyết định kiến trúc ở đây đã được xác nhận và KHÔNG được thay đổi tùy tiện.

---

## 1. THÔNG TIN TỔNG QUAN

| Thuộc tính | Giá trị |
|-----------|---------|
| **Tên dự án** | Swing into the Abyss |
| **Thể loại** | 2D Turn-based Arena Roguelite |
| **Môn học** | Thiết kế Hướng Đối Tượng (OOD) — Đồ án giữa kỳ |
| **Tech stack** | Java SE + Java Swing (Graphics2D). Tuyệt đối KHÔNG dùng game engine/framework ngoài. |
| **Phạm vi (Scope)** | MVP: vòng lặp chiến đấu cốt lõi. Không làm inventory, không làm progression phức tạp. |
| **IDE** | Eclipse (quản lý Java Project, build path) + VS Code/Antigravity (code AI-assisted) |

---

## 2. CẤU TRÚC THƯ MỤC ĐẦY ĐỦ

```
SwingIntoTheAbyss/
├── src/swingabyss/                  ← Root package
│   ├── Main.java                    ✅ DONE — Composition Root, SwingUtilities.invokeLater
│   ├── utils/
│   │   ├── Constants.java           ✅ DONE — hằng số, path asset, frame 1280x720
│   │   └── SpriteLoader.java        ✅ DONE — Singleton + Flyweight + Nearest-Neighbor
│   ├── view/
│   │   ├── MainFrame.java           ✅ DONE — JFrame 1280x720, fixed size
│   │   ├── GamePanel.java           ✅ DONE — arena rendering
│   │   ├── NineSlicePanel.java      ✅ DONE — thuật toán 9-slice
│   │   ├── UIPanel.java             ✅ DONE — action bar parchment
│   │   └── SpriteAnimator.java      ✅ DONE — Swing Timer, frame cycling
│   ├── model/                       ✅ DONE — Entity, Hero, Monster, Stats, Observer
│   ├── controller/                  ✅ DONE — ICommand, Attack, Defend, Heal Command
│   ├── manager/                     ✅ DONE — TurnManager (FSM), GameState
│   └── factory/                     ⬜ CHƯA LÀM — MonsterFactory, RewardFactory
├── assets/                          ✅ DEPLOYED — Đây là folder chứa assets THỰC TẾ dùng trong game
│   ├── heroes/
│   │   ├── knight                   ← Assets tĩnh & động của Knight (spritesheets/folders)
│   │   ├── swordswoman              ← Assets của Swordswoman
│   │   └── magician                 ← Assets của Magician
│   ├── monsters/
│   │   ├── demon                    ← Assets của quái vật Demon
│   │   ├── dragon
│   │   ├── ghost
│   │   ├── beast
│   │   ├── goblin
│   │   └── ogre
│   ├── vfx/                         ← Chứa các hiệu ứng (hit, death, magic...)
│   └── ui/book/
│       ├── book_cover.png           224×160 — dùng làm 9-slice frame cho UIPanel
│       ├── book_page_left.png       parchment trang trái (chưa dùng)
│       ├── slot.png                 30×30   — 9-slice button background
│       ├── bar_frame.png            62×4    — HP bar overlay frame
│       ├── fill.png                 fill utility (không dùng — HP bar dùng Graphics2D color)
│       └── fill_red.png             fill utility (không dùng)
├── stolen/                          ← Asset gốc + tài liệu thiết kế (đừng xóa)
│   ├── Complete_UI_Book_Styles_Pack_Free_v1.0/
│   ├── Class_Diagram.puml           ← UML class diagram (PlantUML)
│   ├── Sequence_Diagram.puml        ← UML sequence diagram
│   └── diagrams/                    ← đã có ảnh Usecase diagram bên trong uc_01.jpg
├── bin/                             ← .class files output (Eclipse / javac -d bin/)
├── UI_Algorithm.md                  ✅ MỚI — Tài liệu kỹ thuật đầy đủ về rendering algorithms
└── SwingIntoTheAbyss_Context.md     ← FILE NÀY
```

---

## 3. CƠ CHẾ GAME CỐT LÕI

- **Fixed Party:** 3 Hero cố định (Knight, Swordswoman, Magician) vs. các đợt Monster (demon, dragon, ghost, beast, goblin, ogre).
- **Turn-based combat:** Thứ tự đánh theo chỉ số Speed. Hero đánh trước, rồi đến Monster.
- **3 Actions/turn**: Attack | Defend | Heal — mỗi cái là 1 `ICommand` object.
  - *Note*: Thay vì dùng cơ chế kho đồ (Item) phức tạp, hành động hồi máu sẽ được thiết kế dạng kỹ năng "Heal" trực tiếp (có thẻ giới hạn số lần charge) để giữ tính chiến thuật nhưng tối giản hóa kiến trúc game mạch lạc hơn.
- **Roguelite:** Sau mỗi Wave, chọn 1/3 phần thưởng ngẫu nhiên (buff/skill mới).
- **Permadeath:** Toàn bộ hero chết → game over, reset.

---

## 4. DESIGN PATTERNS ĐÃ ĐẶT RA (KIẾN TRÚC OOD)

Đây là core OOD của dự án, cần implement nghiêm ngặt. Không được "bỏ qua" pattern vì ngại phức tạp.

### Các Pattern BẮT BUỘC (đã được thầy approve trong đề):

| Pattern | Class chịu trách nhiệm | Mô tả |
|---------|----------------------|-------|
| **Singleton** | `SpriteLoader` | ✅ DONE — Instance duy nhất để cache ảnh |
| **Flyweight** | `SpriteLoader.cache` | ✅ DONE — HashMap cache tránh đọc disk lại |
| **Observer** | `Entity` → `Observer` | ✅ DONE — Entity.notifyObservers() kích hoạt cập nhật UI |
| **Command** | `ICommand`, `AttackCommand`... | ✅ DONE — Đóng gói logic đánh/thủ/hồi máu |
| **State/FSM** | `TurnManager`, `GameState` | ✅ DONE — FSM điều phối vòng lặp game hoàn chỉnh |
| **Strategy** | `ISkill` | ✅ DONE (Khung) — Hero chứa List<ISkill> để đổi chiêu |
| **Composition** | `Stats` ghép vào `Entity` | ✅ DONE — Tách thuộc tính tĩnh khỏi logic động |
| **Factory** | `MonsterFactory` | ⬜ PENDING — Sinh monster ngẫu nhiên theo Wave |
| **MVC** | `model/` ↔ `controller/` | ✅ DONE — Tách rời logic cốt lõi khỏi View |

### Sequence Flow đã được thiết kế (từ `Sequence_Diagram.puml`):
```
Player click "Attack"
  → UIPanel tạo AttackCommand(hero, target)
  → pushCommand vào TurnManager.commandQueue
  → TurnManager.processNextTurn()
  → command.execute()
  → monster.takeDamage(damage)
  → monster.notifyObservers()
  → GamePanel.onNotify(monster) → repaint() [vẽ lại HP bar]
  → TurnManager.changeState(CHECK_TURN)
```

---

## 5. ASSETS — LƯU Ý QUAN TRỌNG CHO AI AGENT

### ✅ Đã hoàn chỉnh hệ thống nạp song song (Spritesheet & Folder Animation):

```java
// Constants.java lưu cả đường dẫn kiểu mảng (cho spritesheet) và đường dẫn gốc (cho folder nạp tự động)
PATH_HERO_KNIGHT_IDLE = "/assets/heroes/Knight/spritesheets/idle_spritesheet.png";
PATH_MONSTER_DEMON_IDLE = "/assets/monsters/demon/sprites/idle";
KNIGHT_IDLE_FRAMES = { 96, 84, 7 }   // Hệ thống tính cứng cho spritesheet: khung rộng 96, 7 frames
```
*Hệ thống nạp được hai chuẩn cấu trúc do Designer xây dựng (ví dụ: `frame1, frame2` cho attack vfx rời, tĩnh là `idle1, idle2`, hoặc sheet liền nạp kiểu cơ bản).*

### ⚠️ CẢNH BÁO — Các cấu trúc gốc, đừng lặp lại:

1. **Khi gán frame cho Spritesheet: đếm frames bằng mắt TRƯỚC, rồi mới chia toán học không được chia mù.** Ví dụ: Knight idle sheet 672px chia 7 frame -> 96px width, tránh chia mù 8 frame 84x84 gây trượt khung.

2. **Không gọi `getScaledInstance()` cho pixel art** — dùng `AffineTransformOp.TYPE_NEAREST_NEIGHBOR` trong SpriteLoader.getScaledPixel().

3. **`SPRITE_SCALE = 1` hiện tại** vì sprites Legacy Collection đã lớn (~80–150px). Nếu dùng sprite nhỏ hơn (16-32px) có thể nâng lên 2 hoặc 3.

4. **`barFill.png` và `barFillRed.png` KHÔNG được dùng** trong HP bar. Fill màu được vẽ bằng `g2d.setColor()` + `fillRect()`. Hai file đó là utility placeholder trong asset pack.

---

## 6. CÁC FILE VIEW ĐÃ HOÀN THÀNH — MÔ TẢ CHI TIẾT

### `Main.java`
```java
SwingUtilities.invokeLater(() -> {
    new MainFrame().setVisible(true);
});
```
Đây là Composition Root. Tất cả ghép nối giữa View-Model-Controller phải xảy ra tại đây khi phases sau được implement.

### `MainFrame.java`
- JFrame 1280x720, `setResizable(false)`, `setLocationRelativeTo(null)` (center màn hình).
- Layout: `BorderLayout` — `GamePanel` ở CENTER (450px), `UIPanel` ở SOUTH (110px).
- UIPanel wrapped trong `JPanel` có `EmptyBorder(4,8,8,8)` để "float" off edge.

### `GamePanel.java`
- Extends `JPanel`, override `paintComponent(Graphics g)`.
- **Rendering pipeline** (thứ tự vẽ Painter's Algorithm):
  1. `drawBackground()` → sky color fill + bgClouds + bgTown + `drawFloor()` (GradientPaint)
  2. `drawEntity()` × n → sprite (SpriteAnimator.getCurrentFrame()) + name text + HP bar
  3. `drawHUD()` → wave/turn info overlay
- Các SpriteAnimator được tạo trong constructor, tự nhận dạng nạp từ Spritesheet lẻ (như Knight) hoặc nạp Folder rời có thuật toán Sort Java Custom (Demon).
- HP bar dùng `AlphaComposite` + `Math.sin(tick * 0.15)` cho pulse effect khi HP < 25%.
- Hiện tại đang test rendering tĩnh hai phe đối lập (Knight vs Demon). Sẽ nhường thuật toán bốc Object qua Model bốc lên.

### `NineSlicePanel.java`
- Extends `JPanel`, override `paintComponent()` gọi `drawNineSlice()`.
- Constructor nhận `(String imagePath, Insets sliceInsets)` — load ảnh qua SpriteLoader.
- `drawNineSlice()` chia 9 vùng, `drawRegion()` dùng `g2d.drawImage(img, dx1,dy1,dx2,dy2, sx1,sy1,sx2,sy2, null)`.
- Default constructor (không arg) → `drawFallback()` vẽ màu tối + border.
- UIPanel extends NineSlicePanel → tự động có parchment frame background.

### `UIPanel.java`
- Extends `NineSlicePanel` với `book_cover.png` và `INSETS_BOOK_COVER = (12,12,12,12)`.
- 4 nút Action: "⚔ Attack", "✨ Skill", "🛡 Defend", "🧪 Item". (Đây là test GUI, thực tế sẽ chỉ có Attack, Defend, Heal)
- Mỗi nút là inner class `ActionButton` (extends JPanel, custom paint với slot.png 9-slice).
- Button có hover effect (màu vàng overlay khi hover) và press offset (x+1, y+2).
- **Command Pattern integration point:** Trong `ActionButton.mouseReleased()` hiện có `action.run()` (Runnable callback). Sau này thay bằng `commandQueue.enqueue(new XxxCommand(...))`.

### `SpriteAnimator.java`
- Field: `BufferedImage[] frames`, `int currentFrame`, `Timer timer`, `boolean flipped`.
- Constructor: gọi `SpriteLoader.getInstance().loadFrames(...)`.
- `javax.swing.Timer` (KHÔNG phải `java.util.Timer`) — fires trên EDT, safe cho Swing.
- Flip: `g2d.drawImage(frame, x + fw, y, -fw, fh, null)` — width âm = mirror ngang.

### `SpriteLoader.java` (utils)
- Singleton: `private static SpriteLoader instance` + `private SpriteLoader()` + `getInstance()`.
- Flyweight: `Map<String, BufferedImage> cache = new HashMap<>()`.
- Load strategy: classpath resource → filesystem fallback (portable cho cả JAR lẫn Eclipse).
- `getScaledPixel()`: `AffineTransformOp(TYPE_NEAREST_NEIGHBOR)` — KHÔNG dùng bilinear.
- `getSubImage()`: `sheet.getSubimage()` — không copy pixel, chỉ là view (shared backing array).
- `makePlaceholder()`: magenta/black checkerboard khi asset không tìm thấy.

### `Constants.java` (utils)
- `public final class Constants` với `private Constants()` — utility class, never instantiated.
- Chứa: window sizes, asset paths (String), frame configs (int[]), 9-slice insets (java.awt.Insets), HP bar colors (java.awt.Color).

---

## 7. TRẠNG THÁI TIẾN ĐỘ HIỆN TẠI

```
GIAI ĐOẠN 1 — Setup & Mockup GUI                 ✅ HOÀN THÀNH
GIAI ĐOẠN 2 — Asset Integration + UI Algorithms  ✅ HOÀN THÀNH
GIAI ĐOẠN 3 — Logic Core & OOD Patterns          ✅ HOÀN THÀNH (MỚI)
GIAI ĐOẠN 4 — UI Polish & Final Integration      ⬜ CHƯA BẮT ĐẦU
```

### Chi tiết Giai đoạn 3 đã hoàn thành (25/04/2026):
- **Giai đoạn 3A (Model):** 
  - Tạo `Stats` bằng **Composition** để chứa các thuộc tính tĩnh.
  - Implement **Observer Pattern** cho `Entity` (mỗi khi mất máu/hồi máu đều gọi `notifyObservers()`).
  - Tạo `ISkill` (**Strategy Pattern**) dọn đường cho các hệ thống kỹ năng phức tạp sau này.
- **Giai đoạn 3B (Controller):**
  - Implement **Command Pattern** với `AttackCommand`, `DefendCommand` (buff 5 giáp), `HealCommand` (có giới hạn Charge).
- **Giai đoạn 3C (Manager - FSM):**
  - Implement **TurnManager** hoạt động như một cỗ máy trạng thái (FSM) xoay vòng từ `CHECK_TURN` -> `HERO_ACTION` / `MONSTER_ACTION`.
  - Bổ sung luồng Main Menu (`MAIN_MENU` -> `START_WAVE` -> ... -> `REWARD_PHASE` / `GAME_OVER`).
  - **Đột phá Thuật toán Lượt đánh:** Giải quyết bài toán Honkai Star Rail UI & Xóa xác chết động (Dynamic Dead Entity Filtering).

---

## 8. THUẬT TOÁN VÀ LOGIC NỔI BẬT ĐÃ GIẢI QUYẾT (GIAI ĐOẠN 3)

### Thuật toán Lọc Xác Chết Động (Dynamic Dead Entity Filtering)
Bài toán: *Làm sao để khi Hero đánh chết Monster A, Monster A lập tức bốc hơi khỏi thanh hiển thị Lượt sắp tới (Turn Order UI) mà không cần code xóa phức tạp?*
- **Cách giải quyết:** 
  1. Khi Monster A hết máu, hàm `takeDamage` gọi `notifyObservers()`.
  2. `GamePanel` (UI) nhận tín hiệu, lập tức tiến hành vẽ lại màn hình.
  3. Trong lúc vẽ lại, UI gọi hàm `TurnManager.getUpcomingTurns(count)`.
  4. Hàm `getUpcomingTurns` được thiết kế cực kỳ thông minh: Quét qua mảng `turnOrder` và nối mảng (Lookahead algorithm), nhưng CỨ ĐỤNG TỚI ai có `isDead() == true` thì vòng lặp tự động **Skip** (nhảy cóc).
  5. Kết quả: Xác chết không bao giờ được trả về cho UI. UI tự dồn người lên trước. Hoàn hảo và sạch sẽ tuyệt đối.

---

## 9. ROADMAP GIAI ĐOẠN 4 — UI POLISH VÀ TÍCH HỢP

Ở Giai đoạn cuối cùng này, chúng ta sẽ đắp da đắp thịt cho game bằng việc nối Core Logic (vừa làm ở Gđ3) vào Swing UI. Các quyết định thiết kế UX/UI sau đây đã được User chốt cứng:

### 9.1 Hệ thống Luồng Game (Flow Menu)
- Mở game lên sẽ không nhảy vào đánh nhau ngay. Cần có giao diện **Main Menu** với 2 nút: `Start Game` và `About`.
- Trong combat, chỉ có 2 nút hệ thống: `Surrender` (Back về Menu) và `Restart` (Chơi lại vòng mới ngay lập tức).

### 9.2 Hệ thống Tooltip (Box Mô Tả Nút Bấm)
- Không dùng tooltip mặc định của Windows.
- Sẽ bắt sự kiện `MouseListener` (Hover). Khi chuột đưa vào nút `Heal` hoặc `Attack`, `UIPanel` sẽ tự tay vẽ (`Graphics2D.drawString`) một khung giấy da (parchment box) nổi lên cung cấp thông tin kỹ năng (Sát thương, Lượt sạc, v.v.).

### 9.3 Hệ thống Bảng Thông Số Quái (Enemy Info Box)
- Sử dụng phong cách **Darkest Dungeon**.
- Góc trên màn hình sẽ có một Box tĩnh. Khi người chơi Hover/Click vào một quái vật, Box này sẽ cập nhật ngay Tên, HP, Attack, Defense của quái đó.

### 9.4 Thanh Thứ Tự Đánh (Honkai Star Rail Turn Order)
- Sử dụng dữ liệu từ hàm `getUpcomingTurns(count)`.
- Hiển thị danh sách 4-5 nhân vật sẽ hành động tiếp theo (theo hàng dọc bên trái hoặc hàng ngang giữa màn hình). Icon quái chết sẽ tự động bốc hơi nhờ thuật toán Lọc đã giải quyết.

### 9.5 Chỉ báo Trực quan (Visual Markers)
- **Turn Indicator:** Có một dấu mũi tên hoặc vòng sáng nhấp nháy trên đầu Hero/Monster đang tới lượt.
- **Target Indicator:** Khi đang chọn mục tiêu đánh, Hover chuột vào quái nào thì đầu quái đó hiện Mũi Tên Đỏ (Red Crosshair) để chống bấm nhầm.

### 9.6 Tích Hợp Command vào ActionButton
```java
// Trong UIPanel:
btnAttack.addActionListener(e -> {
    Entity target = turnManager.getSelectedTarget();
    turnManager.pushCommand(new AttackCommand(currentHero, target));
});

---

## 9. KẾ HOẠCH GHÉP NỐI LOGIC VÀO GUI (QUAN TRỌNG CHO AI AGENT TƯƠNG LAI)

Đây là phần **THEN CHỐT** sau khi có Model + Controller + UML diagrams hoàn chỉnh.

### 9.1 Thay mock data bằng Entity thực

Hiện tại `GamePanel` dùng (cho minh họa test Render mới):
```java
private int[] hpKnight = { 80, 100 };  // MOCK
private int[] hpDemon   = { 70, 120 };  // MOCK
```

Sau khi có Model, thay bằng:
```java
private List<Hero>    heroes;    // reference từ TurnManager
private List<Monster> monsters;  // reference từ TurnManager
```

`drawEntity()` sẽ đọc `hero.getCurrentHp()` và `hero.getMaxHp()` thay vì array cứng.

### 9.2 Đăng ký GamePanel làm Observer

Sau khi có TurnManager:
```java
// Trong Main.java (Composition Root):
TurnManager tm = new TurnManager(heroes, monsters);
GamePanel gp   = new GamePanel(heroes, monsters);  // truyền tham chiếu

// Đăng ký GamePanel observe mọi entity
for (Hero h    : heroes)   h.addObserver(gp);
for (Monster m : monsters) m.addObserver(gp);
```

### 9.3 Kết nối UIPanel buttons → CommandQueue

Hiện tại `ActionButton` có stub:
```java
() -> System.out.println("[UIPanel] Action triggered: " + ACTIONS[idx])
```

Sau khi có Controller:
```java
// UIPanel cần nhận reference đến TurnManager
UIPanel uiPanel = new UIPanel(turnManager);

// Trong ActionButton:
() -> {
    Entity target = turnManager.getSelectedTarget();  // target đang được chọn
    Entity actor  = turnManager.getCurrentActor();    // hero đang đến lượt
    ICommand cmd  = commandFactory.create(actionType, actor, target);
    turnManager.pushCommand(cmd);
}
```

### 9.4 Ghép SpriteAnimator với Entity state

Hiện tại tất cả animat đều chạy loop liên tục. Sau khi có logic:
- **IDLE animation**: khi entity chờ đến lượt → loop bình thường
- **ATTACK animation**: khi entity execute command → play 1 lần rồi về idle
- **HURT animation**: khi takeDamage() → flash/shake rồi về idle
- **DEAD animation**: khi isDead() → play once rồi dừng

```java
// SpriteAnimator sẽ cần thêm:
void playOnce(Runnable onComplete);  // play 1 cycle rồi gọi callback
void setAnimation(String animName);  // chuyển sang spritesheet khác (idle/attack/...)
```

Lưu ý: Legacy Collection sprites có thể có nhiều spritesheet riêng cho từng animation type (idle, attack, death). Cần kiểm tra và config thêm trong Constants.

### 9.5 Cập nhật HUD với dữ liệu thực

`drawHUD()` hiện hardcode `"⚡ WAVE 1"` và `"Knight's Action"`. Sau khi có TurnManager:
```java
private void drawHUD(Graphics2D g2d, int W) {
    // Lấy từ TurnManager:
    int waveNumber  = turnManager.getCurrentWave();
    String actorName = turnManager.getCurrentActor().getName();
    GameState state  = turnManager.getCurrentState();
    
    g2d.drawString("⚡ WAVE " + waveNumber, 14, 18);
    g2d.drawString("▶ " + actorName + "'s Turn", W/2 - 110, 18);
}
```

### 9.6 Figma Layout (nếu có)

Nếu user cung cấp mockup Figma hoặc screenshot layout đo pixel:
1. Cập nhật `Constants.java` với tọa độ X, Y chính xác cho từng entity
2. `drawEntity()` trong GamePanel hiện dùng tọa độ cứng (50, 460...) — thay bằng config từ Constants
3. Đảm bảo không overlap khi sprite size thay đổi

---

## 10. BUILD & RUN HƯỚNG DẪN

```powershell
# Từ thư mục project root:
$root = "c:\My Files\NLUni\ObjOrientedDesign\midterm_project\SwingIntoTheAbyss"

# Compile toàn bộ:
$javaFiles = Get-ChildItem "$root\src" -Recurse -Filter "*.java" | % { $_.FullName }
javac -encoding UTF-8 -sourcepath "$root\src" -d "$root\bin" @javaFiles

# Chạy (PHẢI cd về project root để assets/ tìm được):
java -cp "$root\bin" swingabyss.Main
# Hoặc dùng Start-Process để không block:
Start-Process java -ArgumentList "-cp", "$root\bin", "swingabyss.Main" -WorkingDirectory $root
```

**Lưu ý:** Working directory PHẢI là project root (chứa folder `assets/`). SpriteLoader dùng filesystem fallback path `"assets/sprites/wizard.png"` relative to working dir.

---

## 11. TÀI LIỆU LIÊN QUAN

| File | Mô tả |
|------|-------|
| `UI_Algorithm.md` | Giải thích chi tiết mọi thuật toán render: Singleton, Flyweight, Nearest-Neighbor, 9-Slice, HP Bar pulse, Mirror flip |
| `stolen/Class_Diagram.puml` | UML Class diagram — cấu trúc model/controller/manager chưa implement |
| `stolen/Sequence_Diagram.puml` | UML Sequence — luồng Player click Attack → repaint HP bar |
| `stolen/UC_Diagram.puml` | Use case: Start, Combat Action, Next Wave, Reward |
| `stolen/implementation_plan_v2.md` | Kế hoạch gốc giai đoạn trước (tham khảo) |