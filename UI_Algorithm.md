# UI Algorithm — Tài liệu kỹ thuật xử lý ảnh

> **Dự án:** Swing Into The Abyss — OOD Midterm Project  
> **Package:** `swingabyss.utils` / `swingabyss.view`  
> **Mục đích:** Giải thích chi tiết toàn bộ thuật toán và thiết kế
> liên quan đến việc load, cắt, scale, animate và vẽ sprite/UI assets.

---

## Mục lục

1. [Tổng quan kiến trúc rendering](#1-tổng-quan-kiến-trúc-rendering)
2. [Constants.java — Bộ cấu hình trung tâm](#2-constantsjava--bộ-cấu-hình-trung-tâm)
3. [SpriteLoader.java — Singleton + Flyweight + Nearest-Neighbor](#3-spriteloaderjava--singleton--flyweight--nearest-neighbor)
4. [SpriteAnimator.java — Animation Loop với Swing Timer](#4-spriteanimatorjava--animation-loop-với-swing-timer)
5. [NineSlicePanel.java — Thuật toán 9-Slice Scaling](#5-nineslicepaneljava--thuật-toán-9-slice-scaling)
6. [GamePanel.java — Rendering Pipeline & HP Bar Algorithm](#6-gamepaneljava--rendering-pipeline--hp-bar-algorithm)
7. [Bug thực tế: Ogre Animation & bg_tiles](#7-bug-thực-tế-ogre-animation--bg_tiles)
8. [Bảng tổng hợp Design Patterns](#8-bảng-tổng-hợp-design-patterns)

---

## 1. Tổng quan kiến trúc rendering

Mỗi lần màn hình cần vẽ lại, Swing gọi `paintComponent()` trên `GamePanel`. Pipeline vẽ theo thứ tự từ dưới lên (painter's algorithm):

```
paintComponent() được gọi
│
├── drawBackground(g2d, W, H)
│   ├── fillRect → sky color (#1E2318)
│   ├── drawImage(bgClouds) → sky panorama (top 45%)
│   ├── drawImage(bgTown)   → city silhouette (horizon)
│   └── drawFloor()         → gradient stone floor
│
├── drawEntity() × 5 (heroes + monsters)
│   ├── drawImage(currentFrame)   ← từ SpriteAnimator
│   ├── drawString(name)          ← font overhead
│   └── drawHpBar()               ← HP bar algorithm
│
└── drawHUD()
    ├── fillRect (semi-transparent top bar)
    └── drawString (wave/turn info)
```

**Tại sao dùng Painter's Algorithm?**  
Background vẽ trước, entity vẽ sau → entity đè lên background. Nếu vẽ ngược, background sẽ che entity. Đây là kỹ thuật cơ bản nhất của 2D rendering.

> 📖 **Nghiên cứu thêm:** Painter's Algorithm, Z-buffering (cách 3D game giải quyết bài toán tương tự nhưng phức tạp hơn khi các object giao nhau).

---

## 2. Constants.java — Bộ cấu hình trung tâm

**File:** `src/swingabyss/utils/Constants.java`

### Mục đích thiết kế

`Constants` là một **utility class** — class chỉ chứa các hằng số `static final`, không có method logic, không được khởi tạo. Constructor là `private` để ngăn `new Constants()`.

```java
public final class Constants {
    private Constants() {} // Ngăn instantiation
    
    public static final int WINDOW_WIDTH = 800;
    // ...
}
```

**Tại sao cần class này?**  
Nếu không có Constants, con số như `95`, `133`, `6` sẽ xuất hiện rải rác trong code (gọi là *magic numbers*). Khi cần thay đổi, phải tìm kiếm khắp nơi. Constants tạo ra **Single Source of Truth** — 1 chỗ duy nhất cần sửa.

> 📖 **Nghiên cứu thêm:** Magic numbers anti-pattern, principle DRY (Don't Repeat Yourself).

---

### Sprite Frame Config — Cách đọc spritesheet

```java
public static final int[] WIZARD_FRAMES = { 95, 133, 6 };
//                                          ↑    ↑    ↑
//                                       frameW frameH totalFrames
```

Spritesheet là một ảnh dài chứa nhiều frame xếp ngang. Để biết frame size:

```
sheet width = 570px, frames = 6
→ frameWidth = 570 / 6 = 95px

Ví dụ Ogre bug:
  sheet = 640px, ta nhập 8 frames →  640/8 = 80px mỗi frame (SAI)
  Thực tế: nhìn ảnh thấy 5 tư thế → 640/5 = 128px mỗi frame (ĐÚNG)

Bài học: LUÔN đếm frames bằng mắt trước khi chia toán học.
```

---

### 9-Slice Insets

```java
public static final Insets INSETS_BOOK_COVER = new Insets(12, 12, 12, 12);
//                                                          top left bot right
```

`java.awt.Insets` là class của Java lưu 4 số `(top, left, bottom, right)` — thường dùng cho padding/margin/border. Ở đây ta dùng nó để lưu "ranh giới 9-slice" tính bằng pixel từ mép ảnh gốc.

---

## 3. SpriteLoader.java — Singleton + Flyweight + Nearest-Neighbor

**File:** `src/swingabyss/utils/SpriteLoader.java`

### 3.1 Design Pattern: Singleton

**Vấn đề:** Nếu mỗi class tự `ImageIO.read()` file ảnh khi cần, cùng một ảnh sẽ được đọc từ disk nhiều lần, tốn bộ nhớ và I/O.

**Giải pháp:** Chỉ có **một** object `SpriteLoader` tồn tại trong toàn bộ vòng đời ứng dụng.

```java
public class SpriteLoader {
    // 1. Instance duy nhất — static, private
    private static SpriteLoader instance;

    // 2. Constructor private — bên ngoài KHÔNG thể gọi new SpriteLoader()
    private SpriteLoader() {}

    // 3. Điểm truy cập công khai duy nhất
    public static SpriteLoader getInstance() {
        if (instance == null) {          // Lazy init — tạo khi lần đầu cần
            instance = new SpriteLoader();
        }
        return instance;
    }
}
```

**Luồng gọi từ bất kỳ đâu trong code:**
```
GamePanel.loadAssets()
    → SpriteLoader.getInstance()    // Lần 1: tạo object mới
    
UIPanel constructor
    → SpriteLoader.getInstance()    // Lần 2+: trả về cùng object cũ
```

> 📖 **Nghiên cứu thêm:** Singleton Pattern (GoF Design Patterns). Chú ý: Singleton thread-safe cần `synchronized` hoặc `volatile` — code hiện tại bỏ qua vì Swing chạy single-thread trên EDT.

---

### 3.2 Design Pattern: Flyweight Cache

**Vấn đề:** `GamePanel` cần `wizard.png`. `UIPanel` cũng có thể cần `wizard.png`. Nếu mỗi chỗ đọc disk 1 lần → tốn tài nguyên.

**Giải pháp:** SpriteLoader duy trì một **HashMap** làm cache. Lần đầu load: đọc disk, lưu vào cache. Các lần sau: trả về object từ cache (tham chiếu cùng một vùng bộ nhớ, không copy).

```java
private final Map<String, BufferedImage> cache = new HashMap<>();

public BufferedImage loadImage(String path) {
    // Kiểm tra cache trước — Flyweight!
    if (cache.containsKey(path)) {
        return cache.get(path);   // Trả về reference cũ (cùng object)
    }
    
    // Chưa có → load từ disk
    BufferedImage img = ... (đọc file)
    
    cache.put(path, img);  // Lưu vào cache
    return img;
}
```

**Quan hệ với Flyweight Pattern:**  
Trong GoF, Flyweight chia object thành "intrinsic state" (dữ liệu chia sẻ) và "extrinsic state" (context riêng). Ở đây: pixel data của ảnh là intrinsic (một bản duy nhất trong RAM), vị trí vẽ là extrinsic (GamePanel tự quản lý x, y). SpriteLoader chỉ lưu intrinsic state.

> 📖 **Nghiên cứu thêm:** Flyweight Pattern (GoF), HashMap/hashtable complexity O(1) average lookup.

---

### 3.3 Load Strategy: Classpath → Filesystem Fallback

Một file Java có thể được chạy từ 2 môi trường khác nhau:

| Môi trường | Cách load |
|------------|-----------|
| Packaged JAR | `getResourceAsStream("/assets/...")` — tìm trong classpath bên trong JAR |
| Eclipse IDE | File nằm ngoài `bin/` → classpath không thấy → cần đọc từ filesystem |

```java
// Strategy 1: classpath (JAR-friendly)
InputStream stream = getClass().getResourceAsStream(path);

if (stream == null) {
    // Strategy 2: filesystem (Eclipse IDE)
    // "/assets/sprites/wizard.png" → bỏ "/" đầu → "assets/sprites/wizard.png"
    String filePath = path.startsWith("/") ? path.substring(1) : path;
    File file = new File(filePath);  // Tương đối với working directory
    img = ImageIO.read(file);
}
```

Working directory khi chạy bằng `java -cp bin swingabyss.Main` từ thư mục project root = project root → `new File("assets/sprites/wizard.png")` resolve đúng.

> 📖 **Nghiên cứu thêm:** Java ClassLoader, classpath, resource loading, `getClass().getResourceAsStream()` vs `new File()`.

---

### 3.4 getSubImage() — Cắt frame từ spritesheet

```java
public BufferedImage getSubImage(BufferedImage sheet, int x, int y, int w, int h) {
    // Clamp để tránh ArrayIndexOutOfBounds khi config sai
    int safeX = Math.min(x, sheet.getWidth()  - 1);
    int safeY = Math.min(y, sheet.getHeight() - 1);
    int safeW = Math.min(w, sheet.getWidth()  - safeX);
    int safeH = Math.min(h, sheet.getHeight() - safeY);
    return sheet.getSubimage(safeX, safeY, safeW, safeH);
}
```

`BufferedImage.getSubimage(x, y, w, h)` là method có sẵn của Java AWT. Nó **không copy pixel** — trả về một view vào vùng pixel đó của ảnh gốc (shared backing array). Vì thế rất hiệu quả về bộ nhớ.

**Clamping:** Nếu `x + w > sheet.getWidth()`, Java sẽ throw `RasterFormatException`. Code `Math.min()` phòng thủ trường hợp config `FRAMES` sai.

> 📖 **Nghiên cứu thêm:** Java `BufferedImage`, `Raster`, `DataBuffer` — cách Java lưu pixel data trong RAM.

---

### 3.5 getScaledPixel() — Thuật toán Nearest-Neighbor Scaling

**Vấn đề cần giải quyết:**  
Pixel art được thiết kế với từng pixel có ý nghĩa rõ ràng (hard edges, block colors). Khi scale up 2×, 3×, chúng ta muốn mỗi pixel nhỏ → 4 pixel giống hệt (2×2 block). Thay vì: mỗi pixel nhỏ → blend với 4 pixel xung quanh → ảnh bị mờ.

**So sánh hai phương pháp:**

| Phương pháp | Cách hoạt động | Kết quả trên pixel art |
|-------------|---------------|----------------------|
| Bilinear Interpolation | Tính giá trị pixel mới = trung bình có trọng số của 4 pixel lân cận gốc | Mờ, mất edge sắc nét |
| Nearest-Neighbor | Pixel mới = pixel gần nhất trong ảnh gốc (không blending) | Sắc nét, block pixels |

**Code thực tế:**

```java
public BufferedImage getScaledPixel(BufferedImage source, int scale) {
    int newW = source.getWidth()  * scale;
    int newH = source.getHeight() * scale;
    
    // Tạo ảnh đích có kích thước mới, format ARGB (có alpha channel)
    BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
    
    // AffineTransform: ma trận biến đổi hình học 2D
    // getScaleInstance(sx, sy) → scale đều theo cả 2 chiều
    AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
    
    // AffineTransformOp áp dụng transform khi vẽ
    // TYPE_NEAREST_NEIGHBOR = không interpolation = lấy pixel gần nhất
    AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    
    // filter(src, dst): vẽ src → dst qua phép biến đổi đã định nghĩa
    op.filter(source, scaled);
    
    return scaled;
}
```

**Tại sao không dùng `getScaledInstance()`?**  
`image.getScaledInstance(w, h, Image.SCALE_FAST)` là method cũ của Java, dùng bilinear làm default dù ghi `SCALE_FAST`. `AffineTransformOp` kiểm soát rõ ràng hơn và nhanh hơn vì xử lý trong RAM ngay, không lazy.

> 📖 **Nghiên cứu thêm:**  
> - **AffineTransform** — ma trận biến đổi 2D affine (translate, rotate, scale, shear)  
> - **Image interpolation algorithms** — Nearest Neighbor, Bilinear, Bicubic, Lanczos  
> - **AffineTransformOp** — Java2D API để áp dụng AffineTransform lên BufferedImage

---

### 3.6 loadFrames() — Pipeline hoàn chỉnh

```java
public BufferedImage[] loadFrames(String path, int frameW, int frameH,
                                   int totalFrames, int scale) {
    BufferedImage sheet = loadImage(path);          // 1. Load/cache sheet
    BufferedImage[] frames = new BufferedImage[totalFrames];
    
    for (int i = 0; i < totalFrames; i++) {
        // 2. Cắt frame thứ i: offset x = i * frameW
        BufferedImage raw = getSubImage(sheet, i * frameW, 0, frameW, frameH);
        
        // 3. Scale nếu cần (scale=1 → không scale, trả nguyên)
        frames[i] = (scale > 1) ? getScaledPixel(raw, scale) : raw;
    }
    return frames;
}
```

Luồng xử lý cho `wizard.png` (570×133, 6 frames, scale=1):

```
loadFrames("/assets/sprites/wizard.png", 95, 133, 6, 1)
├── loadImage() → cache miss → đọc disk → BufferedImage 570×133
├── loop i=0: getSubImage(sheet, 0,   0, 95, 133) → frame 0 (wizard idle)
├── loop i=1: getSubImage(sheet, 95,  0, 95, 133) → frame 1
├── loop i=2: getSubImage(sheet, 190, 0, 95, 133) → frame 2
├── ... (scale=1, không gọi getScaledPixel)
└── return BufferedImage[6]
```

**Điểm mấu chốt:** `sheet` vẫn trong cache, nhưng các `getSubimage()` là view vào `sheet` — `frames[i]` là một lightweight reference, không chiếm bộ nhớ mới.

---

### 3.7 makePlaceholder() — Debug helper

```java
private BufferedImage makePlaceholder(int w, int h) {
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = img.createGraphics();  // Vẽ lên BufferedImage bằng Graphics2D
    
    for (int py = 0; py < h; py += 8) {
        for (int px = 0; px < w; px += 8) {
            // Checker pattern: (column + row) chẵn → magenta, lẻ → black
            g.setColor(((px / 8 + py / 8) % 2 == 0)
                    ? new Color(0xFF00FF)   // Magenta — màu "error" truyền thống
                    : Color.BLACK);
            g.fillRect(px, py, 8, 8);
        }
    }
    g.dispose();  // QUAN TRỌNG: giải phóng native graphics resource
    return img;
}
```

Magenta/black checkerboard là convention từ game dev (Source Engine, Unity) để đánh dấu texture lỗi — màu xấu đến mức không thể nhầm lẫn với asset thật.

**`g.dispose()` tại sao bắt buộc?**  
`Graphics2D` nắm một số native resource của OS (font context, paint context...). Nếu không `dispose()`, JVM sẽ giải phóng qua GC nhưng không đảm bảo thời điểm → có thể leak.

---

## 4. SpriteAnimator.java — Animation Loop với Swing Timer

**File:** `src/swingabyss/view/SpriteAnimator.java`

### 4.1 Cấu trúc tổng quan

```java
public class SpriteAnimator {
    private final BufferedImage[] frames;  // Mảng frames đã cắt sẵn
    private int currentFrame = 0;          // Index frame hiện tại
    private final Timer timer;             // Swing Timer tự động tăng frame
    private boolean flipped = false;       // Flag mirror ngang cho monster
}
```

SpriteAnimator **không là JComponent** — nó là plain Java object. Nó không tự vẽ lên màn hình; nó chỉ quản lý trạng thái animation và cung cấp `getCurrentFrame()` để `GamePanel.paintComponent()` lấy về vẽ.

---

### 4.2 javax.swing.Timer — Tại sao không dùng Thread?

**Vấn đề cốt lõi của Swing threading:**  
Swing là **single-threaded** — tất cả UI update PHẢI xảy ra trên Event Dispatch Thread (EDT). Nếu thread khác gọi `repaint()` hay thay đổi component state → race condition → UI glitch hoặc crash.

**Cách giải quyết: `javax.swing.Timer`**  
`javax.swing.Timer` đặc biệt khác `java.util.Timer`: nó fire `ActionEvent` **trên EDT**, không phải thread riêng.

```java
this.timer = new Timer(Constants.ANIM_DELAY_MS, e -> {
    // Đây chạy trên EDT — an toàn 100%
    currentFrame = (currentFrame + 1) % frames.length;
    if (onTick != null) onTick.run();  // onTick = () -> panel.repaint()
});
```

**Luồng hoạt động:**
```
Swing EDT (main loop)
├── xử lý input events
├── paint components
└── xử lý Timer events
    └── Timer fires sau 180ms
        ├── currentFrame = (currentFrame + 1) % 6  → tăng frame
        └── panel.repaint()   → lên schedule lại paintComponent()
```

`% frames.length` là phép modulo → khi `currentFrame` đến `frames.length - 1`, lần sau trở về 0 → vòng lặp vô tận.

> 📖 **Nghiên cứu thêm:**  
> - **EDT (Event Dispatch Thread)** — Swing threading model  
> - **`SwingUtilities.invokeLater()`** — cách giao việc cho EDT từ thread khác  
> - **`javax.swing.Timer` vs `java.util.Timer`** — sự khác biệt quan trọng

---

### 4.3 Flip — Mirror sprite ngang

```java
// Trong GamePanel.drawEntity():
if (animator.isFlipped()) {
    g2d.drawImage(frame, x + fw, y, -fw, fh, null);
    //                   ↑         ↑
    //            điểm bắt đầu  width âm = mirror
} else {
    g2d.drawImage(frame, x, y, null);
}
```

`drawImage(img, x, y, width, height, observer)`: khi `width` là số âm, Java vẽ ảnh từ phải sang trái → hiệu ứng mirror ngang. Kỹ thuật này:
- Không cần tạo ảnh mirror mới (tiết kiệm bộ nhớ)
- Không cần `AffineTransform` tường minh
- Hiệu suất tương đương drawImage thường

> 📖 **Nghiên cứu thêm:** `Graphics.drawImage()` overloads, negative dimensions behavior.

---

### 4.4 Vòng đời animator (lifecycle)

```java
heroWizard.start();    // Timer bắt đầu chạy
heroWizard.pause();    // Timer dừng, giữ currentFrame
heroWizard.resume();   // Timer tiếp tục
heroWizard.stop();     // Timer dừng, reset về frame 0
```

`pause()`/`resume()` dự kiến dùng khi TurnManager chuyển turn — freeze animation của entity đang không hoạt động. Đây là integration point để ghép với State/Turn logic sau này.

---

## 5. NineSlicePanel.java — Thuật toán 9-Slice Scaling

**File:** `src/swingabyss/view/NineSlicePanel.java`

### 5.1 Vấn đề: Tại sao không scale bình thường?

Hãy tưởng tượng ảnh khung UI có 4 góc được thiết kế chi tiết (vd: góc vàng của book_cover). Nếu stretch toàn ảnh theo kích thước panel:

```
Ảnh gốc 224×160:          Scale nguyên thành 800×110:
┌────╮  ╭────┐             ┌──────────────────────────────╮╭──────┐
│ góc│  │góc │             │ góc bị kéo dài               ││bị kéo│
│ OK │  │ OK │   →         │ méo mó                       ││ méo  │
│    │  │    │             │                              ││      │
└────╯  ╰────┘             └──────────────────────────────╯╰──────┘
```

4 góc bị kéo dãn, mất shape → xấu.

**Giải pháp 9-Slice:** Chia ảnh thành 9 vùng, mỗi vùng scale theo cách riêng.

---

### 5.2 Chia vùng — Định nghĩa ranh giới bằng Insets

```
Source image 224×160 với insets = (12, 12, 12, 12):

              sL=12        sR=212
               ↓            ↓
  sT=12 → ┌──────┬────────────┬──────┐
           │  TL  │  Top Edge  │  TR  │
           ├──────┼────────────┼──────┤
           │ Left │  Center    │Right │
           │ Edge │            │ Edge │
           ├──────┼────────────┼──────┤
  sB=148 → │  BL  │ Bottom Edg │  BR  │
           └──────┴────────────┴──────┘

  sL = insets.left = 12
  sT = insets.top  = 12
  sR = width - insets.right = 224 - 12 = 212
  sB = height - insets.bottom = 160 - 12 = 148
```

---

### 5.3 Code thuật toán — drawNineSlice()

```java
private void drawNineSlice(Graphics2D g2d, int dx, int dy, int dw, int dh) {

    // Tắt interpolation — pixel art UI cần sắc nét
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

    final int sw = sourceImage.getWidth();   // source width
    final int sh = sourceImage.getHeight();  // source height

    // ── Ranh giới trên SOURCE (ảnh gốc) ──
    final int sL = sliceInsets.left;          // 12
    final int sT = sliceInsets.top;           // 12
    final int sR = sw - sliceInsets.right;    // 224 - 12 = 212
    final int sB = sh - sliceInsets.bottom;   // 160 - 12 = 148

    // ── Ranh giới trên DESTINATION (panel được vẽ) ──
    final int dL = sliceInsets.left;          // 12 (góc giữ nguyên kích thước)
    final int dT = sliceInsets.top;           // 12
    final int dR = dw - sliceInsets.right;    // panelW - 12 (co giãn phần giữa)
    final int dB = dh - sliceInsets.bottom;   // panelH - 12

    // Vẽ 9 vùng: mỗi vùng là 1 lệnh drawImage với source rect và dest rect khác nhau
    drawRegion(g2d,  dx,     dy,     dL,    dT,    0,  0,  sL,    sT   );  // TL corner
    drawRegion(g2d,  dx+dL,  dy,     dR-dL, dT,    sL, 0,  sR-sL, sT   );  // Top edge
    drawRegion(g2d,  dx+dR,  dy,     dw-dR, dT,    sR, 0,  sw-sR, sT   );  // TR corner
    
    drawRegion(g2d,  dx,     dy+dT,  dL,    dB-dT, 0,  sT, sL,    sB-sT);  // Left edge
    drawRegion(g2d,  dx+dL,  dy+dT,  dR-dL, dB-dT, sL, sT, sR-sL, sB-sT); // CENTER
    drawRegion(g2d,  dx+dR,  dy+dT,  dw-dR, dB-dT, sR, sT, sw-sR, sB-sT); // Right edge
    
    drawRegion(g2d,  dx,     dy+dB,  dL,    dh-dB, 0,  sB, sL,    sh-sB);  // BL corner
    drawRegion(g2d,  dx+dL,  dy+dB,  dR-dL, dh-dB, sL, sB, sR-sL, sh-sB); // Bottom edge
    drawRegion(g2d,  dx+dR,  dy+dB,  dw-dR, dh-dB, sR, sB, sw-sR, sh-sB); // BR corner
}
```

**4 góc (TL, TR, BL, BR):** source width = `sL` (12px), dest width = `dL` (12px) → giống nhau → không scale → góc không méo.

**Top/Bottom edge:** source height = `sT` (12px), dest height = `dT` (12px) → không scale theo chiều dọc. Source width = `sR-sL` (200px), dest width = `dR-dL` (panelW - 24) → scale theo chiều ngang.

**Left/Right edge:** Ngược lại — scale dọc, giữ ngang.

**Center:** Scale cả 2 chiều — đây là phần lớn nhất, không có detail quan trọng nên OK khi kéo giãn.

---

### 5.4 drawRegion() — Vẽ một ô trong 9

```java
private void drawRegion(Graphics2D g2d,
                         int dx, int dy, int dw, int dh,   // destination rect (x,y,w,h)
                         int sx, int sy, int sw, int sh) {  // source rect (x,y,w,h)
    
    if (dw <= 0 || dh <= 0 || sw <= 0 || sh <= 0) return;  // skip vùng zero-size
    
    // Java drawImage dùng tọa độ x1,y1,x2,y2 (điểm đầu và cuối)
    // nên convert từ x,y,w,h → x1,y1,x2,y2
    g2d.drawImage(sourceImage,
            dx,      dy,      dx + dw, dy + dh,   // destination: (x1,y1) → (x2,y2)
            sx,      sy,      sx + sw, sy + sh,   // source:      (x1,y1) → (x2,y2)
            null);                                 // ImageObserver (null = không cần)
}
```

`Graphics2D.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)` là overload đặc biệt: nó **scale ảnh** từ source rect sang dest rect **tự động**. Đây là nơi scaling thực sự xảy ra — Java AWT lo phần toán học.

> 📖 **Nghiên cứu thêm:**  
> - **9-Slice / 9-patch scaling** — kỹ thuật phổ biến trong UI framework (Android 9-patch, Unity Sprite Editor, CSS border-image)  
> - **`Graphics2D.drawImage()` overloads** — Java2D API  
> - **`RenderingHints`** — cách control chất lượng render trong Java2D

---

### 5.5 Guard case — Panel nhỏ hơn insets

```java
if (dR <= dL || dB <= dT) {
    g2d.drawImage(sourceImage, dx, dy, dw, dh, null);
    return;
}
```

Nếu panel nhỏ hơn `2 * insets` (vd panel 10px mà insets=12px), `dR - dL` sẽ âm → 9-slice vô nghĩa. Fallback: kéo toàn bộ ảnh vào panel nhỏ.

---

## 6. GamePanel.java — Rendering Pipeline & HP Bar Algorithm

**File:** `src/swingabyss/view/GamePanel.java`

### 6.1 Background layers — Tại sao không dùng bg_tiles?

Project có 3 background assets:
- `bg_clouds.png` — bầu trời: **dùng được** — là ảnh panorama hoàn chỉnh
- `bg_town.png` — thành phố silhouette: **dùng được** — ảnh với kênh alpha transparent
- `bg_tiles.png` — **KHÔNG dùng được làm background**

**`bg_tiles.png` là gì?** Nhìn vào ảnh: một grid chứa nhiều tiles nhỏ khác nhau (gạch đá, torch, cửa, cửa sổ...). Đây là **tileset** hay **tile atlas** — asset được thiết kế để engine lấy từng tile riêng lẻ bằng tọa độ grid, rồi lặp lại (tile) để xây map.

**Cách dùng đúng tileset:**
```
Muốn vẽ sàn đá: lấy tile gạch tại (col=0, row=0) trong tileset
Kích thước mỗi tile = 16px
→ getSubImage(tileset, 0*16, 0*16, 16, 16)
→ repeat vẽ tile này N lần để lấp đầy chiều rộng

→ Thuộc về class TileMapRenderer (chưa implement, out-of-scope cho giai đoạn này)
```

**Cách dùng sai (đã làm lúc đầu):** `drawImage(bgTiles, 0, 0, W, H)` → kéo toàn bộ atlas 500px lên fill panel 800px → trông như collage tile atlas bị méo.

**Giải pháp hiện tại:** Floor được vẽ thuần Graphics2D:
```java
java.awt.GradientPaint floorGrad = new java.awt.GradientPaint(
    0, floorY, new Color(0x2C2820),  // top: tối
    0, H,      new Color(0x1A1512)   // bottom: tối hơn
);
g2d.setPaint(floorGrad);
g2d.fillRect(0, floorY, W, H - floorY);
```

`GradientPaint` tạo gradient tuyến tính giữa 2 điểm — phù hợp hơn cho nền arena.

> 📖 **Nghiên cứu thêm:** Tileset/Tilemap rendering, **Tiled Map Editor** (công cụ phổ biến), GradientPaint vs TexturePaint trong Java2D.

---

### 6.2 HP Bar Algorithm

HP bar là một bài toán rendering nhỏ nhưng hoàn chỉnh:

```java
private void drawHpBar(Graphics2D g2d, int x, int y, int w, int current, int max) {
    final int BAR_H = 8;
    
    // BƯỚC 1: Tính ratio (0.0 → 1.0), clamp để tránh overflow
    float ratio = Math.max(0f, Math.min(1f, (float) current / max));
    //            ↑ min 0       ↑ max 1       ↑ ép kiểu float để tránh integer division
    
    // BƯỚC 2: Vẽ nền tối (empty bar)
    g2d.setColor(new Color(20, 10, 10, 200));  // alpha=200 → bán trong suốt
    g2d.fillRect(x, y, w, BAR_H);
    
    // BƯỚC 3: Chọn màu theo ngưỡng
    if (ratio > 0.50f)  → GREEN   (HP ổn)
    if (ratio > 0.25f)  → YELLOW  (HP thấp)
    else                → RED     (HP nguy hiểm + pulse effect)
    
    // BƯỚC 4: Vẽ fill (chiều rộng theo ratio)
    int fillW = Math.max(2, (int)(w * ratio));  // min 2px để luôn thấy bar
    g2d.fillRect(x, y, fillW, BAR_H);
    
    // BƯỚC 5: Overlay frame ảnh UI lên trên fill
    g2d.drawImage(barFrame, x, y, w, BAR_H, null);
}
```

**Tại sao `(float) current / max` chứ không phải `current / max`?**  
Trong Java, `int / int = int` (integer division làm tròn xuống). `80 / 100 = 0`. Ép `(float)` trước phép chia → `(float) 80 / 100 = 0.8f`.

---

### 6.3 HP Bar Pulse Effect — AlphaComposite + sin()

```java
// Chỉ kích hoạt khi HP < 25%
float pulse = 0.6f + 0.4f * (float) Math.sin(tick * 0.15);
//            ↑ min alpha    ↑ biên độ   ↑ tần số dao động
//            → dao động từ 0.6 đến 1.0

Composite original = g2d.getComposite();  // Lưu composite cũ
g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
    // SRC_OVER: vẽ source lên trên destination với alpha blend
    // pulse: alpha value (0.0 transparent → 1.0 opaque)
g2d.fillRect(...);       // Vẽ với alpha động
g2d.setComposite(original);  // PHẢI restore lại — nếu không, mọi thứ vẽ sau đều bị alpha!
```

**`tick`** là biến int được tăng mỗi `paintComponent()`. `Math.sin(tick * 0.15)` cho ra sóng hình sin [-1, 1]. Scale thành [0.6, 1.0] bằng `0.6 + 0.4 * sin(...)`.

**Tại sao phải restore Composite?**  
`Graphics2D` là stateful — mọi thứ set (color, font, composite...) sẽ tồn tại cho đến khi set lại. Nếu không restore, các entity vẽ sau sẽ cũng bị alpha thấp.

> 📖 **Nghiên cứu thêm:**  
> - **AlphaComposite** — Porter-Duff compositing rules (SRC_OVER, DST_IN, XOR...)  
> - **`Math.sin()` cho animation** — kỹ thuật dùng hàm lượng giác để tạo hiệu ứng dao động  
> - **Graphics2D state management** — tại sao cần save/restore

---

## 7. Bug thực tế: Ogre Animation & bg_tiles

### Bug 1 — Ogre "kéo ảnh dài"

**Triệu chứng:** Ogre không phát ra animation frame-by-frame mà trông như một ảnh dài đang scroll.

**Root cause:**

```
OGRE_FRAMES = { 80, 128, 8 }  ← SAI

Sheet ogre.png: 640 × 128 px
Thực tế: 5 tư thế (đếm bằng mắt)
→ frameWidth = 640 / 5 = 128px

Khi dùng frameW=80, totalFrames=8:
  frame 0: getSubImage(0,   0, 80, 128) → cắt giữa pose 0 và 1
  frame 1: getSubImage(80,  0, 80, 128) → cắt từ giữa pose 0
  frame 2: getSubImage(160, 0, 80, 128) → bắt đầu pose 1
  ...
Kết quả: mỗi "frame" là một mảnh vá víu của 2 pose liền kề.
Khi animate liên tiếp trông như đang scroll ảnh dài.
```

**Fix:** `OGRE_FRAMES = { 128, 128, 5 }` — Dựa trên thực đo: 640/5=128.

**Bài học:** Khi dùng spritesheet asset bên thứ ba, luôn xem ảnh thực tế và đếm frame trước. Con số từ tên file không đáng tin cậy.

---

### Bug 2 — bg_tiles vẽ sai mục đích

**Triệu chứng:** Phần nền bên dưới trông như bức tranh lạ gồm đủ thứ gạch, cửa, torch bị kéo dãn.

**Root cause:** `drawImage(bgTiles, 0, tilesY, W, H/2, null)` — stretch toàn bộ tileset atlas lên phần nền.

**Fix:** Loại bỏ `bgTiles` khỏi background pipeline. Thay bằng `GradientPaint` cho floor.

---

## 8. Bảng tổng hợp Design Patterns

| Pattern | Class áp dụng | Mô tả ngắn |
|---------|--------------|-----------|
| **Singleton** | `SpriteLoader` | Chỉ 1 instance duy nhất trong toàn app |
| **Flyweight** | `SpriteLoader.cache` | Reuse image object, không load disk nhiều lần |
| **Template Method** *(implicit)* | `NineSlicePanel.paintComponent()` | Định nghĩa skeleton algorithm (drawNineSlice / drawFallback) |
| **Strategy** *(load)* | `SpriteLoader.loadImage()` | 2 chiến lược load: classpath → filesystem fallback |
| **Observer** *(partial)* | `SpriteAnimator` → `onTick` callback | Notify GamePanel khi frame thay đổi |
| **Utility class** | `Constants` | Static-only, no state, no instantiation |

---

## Lưu ý quan trọng khi thảo luận với thầy

1. **`getSubimage()` không copy pixel** — chỉ là logical view (shared backing array). Câu hỏi phản biện: "Điều gì xảy ra nếu ảnh gốc bị GC thu hồi?" → Không thể xảy ra vì `frames[]` còn giữ strong reference đến subimage, subimage giữ strong ref đến parent image.

2. **Flyweight vs Caching** — Đây thực ra là Cache (HashMap). Flyweight pattern chính xác hơn khi tách intrinsic/extrinsic state thành class riêng biệt. Trong project này, ta áp dụng **tinh thần** của Flyweight (chia sẻ object) qua Cache.

3. **Nearest-Neighbor không phải lúc nào cũng tốt** — Với ảnh chụp thật (photorealistic), bilinear/bicubic cho kết quả mượt hơn. Nearest-Neighbor chỉ phù hợp cho pixel art.

4. **`javax.swing.Timer` là polling, không phải interrupt** — Timer không đảm bảo đúng 180ms; nó đặt event vào EDT queue và được xử lý khi EDT rảnh. Nếu EDT busy, frame có thể delay. Cho game turn-based, điều này không ảnh hưởng gameplay.

5. **9-Slice là kỹ thuật chuẩn** — Android SDK có `NinePatchDrawable`, CSS có `border-image`, Unity có Sprite Editor 9-slice mode. Tên gọi khác: "9-patch", "border-image slicing".
