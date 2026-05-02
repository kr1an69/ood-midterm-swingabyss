package swingabyss.view;

import swingabyss.utils.Constants;
import swingabyss.utils.SpriteLoader;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

import swingabyss.model.Entity;
import swingabyss.model.Hero;
import swingabyss.model.Monster;
import swingabyss.model.Observer;
import swingabyss.manager.TurnManager;
import swingabyss.manager.GameState;
import swingabyss.controller.AttackCommand;

/**
 * GamePanel — the main arena where the combat is drawn.
 *
 * Rendering pipeline (executed every repaint() call):
 *  1. Background layers: Gothic Horror parallax (clouds → town → tiles)
 *  2. Entity sprites: Heroes (left side) & Monsters (right side),
 *     each animated via their own SpriteAnimator.
 *  3. HP Bars: drawn above each entity, using the TravelBook bar asset.
 *  4. Turn HUD: wave number + current state overlay at the top.
 *
 * Sprite flipping:
 *   Heroes face right  → drawn normally (no flip)
 *   Monsters face left → drawn with AffineTransform horizontal mirror
 *
 * HP Bar color states:
 *   > 50% HP → green (safe)
 *   25–50%  → yellow (warning)
 *   < 25%   → red (critical, pulses via alpha)
 */
public class GamePanel extends JPanel implements Observer {

    private static final long serialVersionUID = 1L;

    // Background: sky panorama + city silhouette layers only.
    // bg_tiles is a TILESET (individual tile graphics), NOT a scene background -- excluded intentionally.
    private BufferedImage bgClouds, bgTown;

    // ── Hero animators (left side) ──────────────────────────
    private SpriteAnimator heroKnight;

    // ── Monster animators (right side) ─────────────────────
    private SpriteAnimator monsterDemon;

    // HP Bar frame image overlay. Fill is drawn as colored Graphics2D rects (no image needed).
    private BufferedImage barFrame;

    // ── Mock HP values (will be driven by Model in full architecture) ──
    // Format: { currentHp, maxHp }
    private int[] hpKnight = { 80, 100 };
    private int[] hpDemon  = { 70, 120 };

    // ── Animation tick counter (for pulsing HP bar) ─────────
    private int tick = 0;
    
    private TurnManager turnManager;
    private BufferedImage slotImg;
    private BufferedImage pointImg;
    private BufferedImage descFrameImg;
    private static class HitboxRecord {
        Entity entity;
        Rectangle bounds;
        int spriteX;
        int spriteY;
        int spriteW;
        HitboxRecord(Entity e, Rectangle b, int sx, int sy, int sw) {
            this.entity = e;
            this.bounds = b;
            this.spriteX = sx;
            this.spriteY = sy;
            this.spriteW = sw;
        }
    }
    private List<HitboxRecord> entityHitboxes = new ArrayList<>();
    
    private HitboxRecord getHitboxRecord(Entity target) {
        for (HitboxRecord r : entityHitboxes) {
            if (r.entity == target) return r;
        }
        return null;
    }
    private Entity hoveredEntity = null;

    public GamePanel(TurnManager turnManager) {
        this.turnManager = turnManager;
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT));
        setBackground(Color.BLACK);

        loadAssets();
        createAnimators();
        startAllAnimators();
        
        // Register this GamePanel to observe all entities
        for (Hero h : turnManager.getHeroes()) h.addObserver(this);
        for (Monster m : turnManager.getMonsters()) m.addObserver(this);
        
        // Listener chuột
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                hoveredEntity = null;
                // Duyệt ngược từ cuối lên đầu để lấy con nằm trên cùng
                for (int i = entityHitboxes.size() - 1; i >= 0; i--) {
                    HitboxRecord record = entityHitboxes.get(i);
                    if (record.bounds.contains(e.getPoint())) {
                        hoveredEntity = record.entity;
                        break;
                    }
                }
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (turnManager.getCurrentState() == GameState.SELECT_TARGET) {
                    if (hoveredEntity != null && hoveredEntity instanceof Monster && !hoveredEntity.isDead()) {
                        AttackCommand cmd = new AttackCommand(turnManager.getCurrentActor(), hoveredEntity);
                        turnManager.pushCommand(cmd);
                    }
                }
            }
        };
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    public void onNotify(Entity entity) {
        repaint();
    }

    // ─────────────────────────────────────────────────────────
    // ASSET LOADING
    // ─────────────────────────────────────────────────────────

    private void loadAssets() {
        SpriteLoader sl = SpriteLoader.getInstance();

        // Background: only sky panorama + city silhouette are usable as bg layers.
        // bg_tiles is a TILESET (tile collection for map building), not a scene background.
        bgClouds = sl.loadImage(Constants.BG_CLOUDS);
        bgTown   = sl.loadImage(Constants.BG_TOWN);
        // bg_tiles intentionally NOT loaded here — would need a tile-rendering system
        // to use correctly (pick individual tiles by grid position, repeat to fill floor).
        // For now, the floor is a hand-drawn Graphics2D gradient.

        // HP Bar: only the frame overlay is needed as an image.
        // The fill is drawn as a colored rectangle (the fill .png assets are 1x1px utility files).
        barFrame = sl.loadImage(Constants.UI_BAR_FRAME);
        slotImg = sl.loadImage(Constants.UI_SLOT);
        pointImg = sl.loadImage(Constants.UI_POINT);
        descFrameImg = sl.loadImage(Constants.UI_DESCRIPTION_FRAME);
    }

    private void createAnimators() {
        // Heroes — face right (no flip)
        // Uses the original spritesheet loading method
        heroKnight = new SpriteAnimator(
                Constants.PATH_HERO_KNIGHT_IDLE,
                Constants.KNIGHT_IDLE_FRAMES[0], Constants.KNIGHT_IDLE_FRAMES[1],
                Constants.KNIGHT_IDLE_FRAMES[2], Constants.SPRITE_SCALE,
                this::repaint);

        // Monsters — face left (flip horizontally when drawing)
        // Uses spritesheet loading approach
        monsterDemon = new SpriteAnimator(
                Constants.PATH_MONSTER_DEMON_IDLE,
                Constants.DEMON_IDLE_FRAMES[0], Constants.DEMON_IDLE_FRAMES[1],
                Constants.DEMON_IDLE_FRAMES[2], Constants.SPRITE_SCALE,
                this::repaint);
        // Sprite gốc đã quay trái nên không cần flip
        monsterDemon.setFlipped(false);
    }

    private void startAllAnimators() {
        heroKnight.start();
        monsterDemon.start();
    }

    // ─────────────────────────────────────────────────────────
    // PAINT PIPELINE
    // ─────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        entityHitboxes.clear(); // Làm mới danh sách Hitbox mỗi frame
        Graphics2D g2d = (Graphics2D) g;
        tick++;

        // Pixel art: disable smoothing for sprites
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        // But enable antialiasing for text/UI overlays
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int W = getWidth();
        int H = getHeight();

        // 1. Background + floor (drawFloor is called internally by drawBackground)
        drawBackground(g2d, W, H);


        // ── 3. Hero sprites (left side) ────────────────────
        // Ground baseline: H - 70 (a bit above the bottom edge)
        int groundY = H - 70;
        List<Hero> heroes = turnManager.getHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            Hero h = heroes.get(i);
            if (!h.isDead()) {
                // Thêm offset Y để Knight chạm đất (do frame gốc bị hở gót)
                int knightOffsetY = 24; 
                int xPos = 80 + i * 140;
                drawEntity(g2d, heroKnight, xPos, groundY - heroKnight.getFrameHeight() + knightOffsetY, h);
            }
        }

        // ── 4. Monster sprites (right side) ─────────────────
        List<Monster> monsters = turnManager.getMonsters();
        for (int i = 0; i < monsters.size(); i++) {
            Monster m = monsters.get(i);
            if (!m.isDead()) {
                int xPos = W - 200 - i * 140;
                drawEntity(g2d, monsterDemon, xPos, groundY - monsterDemon.getFrameHeight(), m);
            }
        }

        // ── 5. HUD overlay ──────────────────────────────────
        drawHUD(g2d, W);
        
        // ── 6. Turn Order UI ────────────────────────────────
        drawTurnOrder(g2d, W, H);
        
        // ── 7. Pointers & Tooltips ──────────────────────────
        drawPointersAndTooltips(g2d, W, H);
    }

    // ─────────────────────────────────────────────────────────
    // BACKGROUND LAYERS  (parallax — 3 steps, static for turn-based)
    // ─────────────────────────────────────────────────────────

    /**
     * Draws the 2-layer Gothic Horror background:
     *   Layer 1 — Sky fill (#2A2E1F dark greenish-grey matching Gothic Horror palette)
     *   Layer 2 — Cloud panorama (bg_clouds.png) stretched across the top sky area
     *   Layer 3 — Town silhouette (bg_town.png) overlaid above the ground
     *   Layer 4 — Floor (drawn with Graphics2D gradient — NOT bg_tiles which is a tileset)
     *
     * bg_tiles is a TILESET asset: it contains a grid of individual architectural tiles
     * (stone blocks, doors, torches, etc.) meant to be assembled into game maps by picking
     * specific tiles by grid coordinates. Drawing it stretched as a background would be
     * visually wrong (you'd see the whole tile atlas smeared across the screen).
     * A proper tileset renderer requires: tileSize, tileX, tileY, repeat-fill logic.
     * That belongs in a future TileMapRenderer class, not in the background layer.
     */
    private void drawBackground(Graphics2D g2d, int W, int H) {
        // ── Layer 1: Dark sky fill (Gothic Horror atmospheric color) ──
        g2d.setColor(new Color(0x1E2318));
        g2d.fillRect(0, 0, W, H);

        // ── Layer 2: Clouds panorama (top 45% of screen) ──
        if (bgClouds != null) {
            int cloudH = (int)(H * 0.45);
            g2d.drawImage(bgClouds, 0, 0, W, cloudH, null);
        }

        // ── Layer 3: Town silhouette (sits at horizon, ~35% from top) ──
        if (bgTown != null) {
            // Keep aspect ratio of town silhouette, stretch width only
            int townH = (int)(bgTown.getHeight() * ((double) W / bgTown.getWidth()));
            int townY = (int)(H * 0.35) - townH / 2;
            g2d.drawImage(bgTown, 0, townY, W, townH, null);
        }

        // ── Layer 4: Ground floor (Graphics2D drawn — no tileset needed for turn-based) ──
        drawFloor(g2d, W, H);
    }

    /**
     * Draws the arena floor using Graphics2D.
     * A proper Gothic dungeon floor: dark stone-grey gradient.
     * This replaces any tileset-based approach — for a turn-based arena,
     * a solid painted floor is simpler and more appropriate.
     */
    private void drawFloor(Graphics2D g2d, int W, int H) {
        int floorY = H - 70;

        // Stone floor gradient (dark top, slightly lighter going down)
        java.awt.GradientPaint floorGrad = new java.awt.GradientPaint(
            0, floorY, new Color(0x2C2820),
            0, H,      new Color(0x1A1512)
        );
        g2d.setPaint(floorGrad);
        g2d.fillRect(0, floorY, W, H - floorY);

        // Top edge highlight — faint bright line at floor/scene boundary
        g2d.setColor(new Color(0x5C5040));
        g2d.fillRect(0, floorY, W, 2);

        // Shadow strip just above the floor (entities appear to "stand" on it)
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRect(0, floorY - 6, W, 8);
    }

    // ─────────────────────────────────────────────────────────
    // ENTITY DRAWING
    // ─────────────────────────────────────────────────────────

    /**
     * Draws a single entity (hero or monster) with its sprite and HP bar.
     *
     * @param g2d       graphics context
     * @param animator  the entity's SpriteAnimator
     * @param x         left edge of the sprite (world x)
     * @param y         top edge of the sprite (world y)
     * @param hp        {currentHp, maxHp} array
     * @param name      display name drawn above HP bar
     */
    private void drawEntity(Graphics2D g2d, SpriteAnimator animator,
                             int x, int y, Entity e) {
        BufferedImage frame = animator.getCurrentFrame();
        int fw = animator.getFrameWidth();
        int fh = animator.getFrameHeight();

        // Lưu Hitbox cho Mouse Listener (bóp nhỏ lại để tránh đè viền trong suốt)
        int padX = 35;
        int padY = 20;
        int hitX = x + padX;
        int hitY = y + padY;
        int hitW = Math.max(10, fw - 2 * padX);
        int hitH = Math.max(10, fh - 2 * padY);
        entityHitboxes.add(new HitboxRecord(e, new Rectangle(hitX, hitY, hitW, hitH), x, y, fw));

        // Draw sprite — mirror horizontally for flipped entities (monsters)
        if (animator.isFlipped()) {
            g2d.drawImage(frame, x + fw, y, -fw, fh, null);
        } else {
            g2d.drawImage(frame, x, y, null);
        }

        // Draw entity name above sprite
        g2d.setFont(new Font("Monospaced", Font.BOLD, 10));
        g2d.setColor(new Color(0xF5E6C8));
        FontMetrics fm = g2d.getFontMetrics();
        int nameX = x + (fw - fm.stringWidth(e.getName())) / 2;
        g2d.drawString(e.getName(), nameX, y - 22);

        // Draw HP bar above sprite
        int barW = 60; // Cố định chiều rộng thanh máu
        int barX = x + (fw - barW) / 2;
        int barY = y - 14;
        drawHpBar(g2d, barX, barY, barW, e.getCurrentHp(), e.getStats().getMaxHp());
    }

    // ─────────────────────────────────────────────────────────
    // HP BAR RENDERING ALGORITHM
    // ─────────────────────────────────────────────────────────

    /**
     * Draws a pixel-art HP bar at the given position.
     *
     * Algorithm:
     *   1. Calculate ratio = currentHp / maxHp (float, clamped 0–1)
     *   2. Choose fill color based on ratio thresholds
     *   3. Draw background (empty bar)
     *   4. Draw fill rectangle scaled to ratio × barW
     *   5. Overlay the bar frame asset via simple horizontal stretch
     *   6. For critical HP (< 25%): apply pulsing alpha effect via sin(tick)
     *
     * @param g2d       graphics context
     * @param x         left edge of the bar in panel coordinates
     * @param y         top edge of the bar in panel coordinates
     * @param w         total width of the bar
     * @param current   current HP value
     * @param max       maximum HP value
     */
    private void drawHpBar(Graphics2D g2d, int x, int y, int w, int current, int max) {
        final int BAR_H = 8;

        // Clamp ratio
        float ratio = Math.max(0f, Math.min(1f, (float) current / max));

        // ── Background (empty portion) ──
        g2d.setColor(new Color(20, 10, 10, 200));
        g2d.fillRect(x, y, w, BAR_H);

        // ── Choose fill color based on HP % ──
        Color fillColor;
        if (ratio > 0.5f) {
            fillColor = Constants.COLOR_HP_GREEN;
        } else if (ratio > 0.25f) {
            fillColor = Constants.COLOR_HP_YELLOW;
        } else {
            // Critical HP: pulse the alpha using time for visual urgency (prevents stutter on mouse move)
            fillColor = Constants.COLOR_HP_RED;
            long time = System.currentTimeMillis();
            float pulse = 0.6f + 0.4f * (float) Math.sin(time / 150.0);
            Composite original = g2d.getComposite();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));
            int fillW = Math.max(2, (int)(w * ratio));
            g2d.setColor(fillColor);
            g2d.fillRect(x, y, fillW, BAR_H);
            g2d.setComposite(original);
            drawBarFrame(g2d, x, y, w, BAR_H);
            return; // Early return after handling pulse
        }

        // ── Normal HP fill ──
        int fillW = Math.max(2, (int)(w * ratio));
        g2d.setColor(fillColor);
        g2d.fillRect(x, y, fillW, BAR_H);

        // ── Bar frame overlay (TravelBook bar asset, stretched horizontally) ──
        drawBarFrame(g2d, x, y, w, BAR_H);
    }

    /**
     * Draws the HP bar frame asset on top of the fill.
     * The bar asset is a thin horizontal strip — we stretch it to width.
     */
    private void drawBarFrame(Graphics2D g2d, int x, int y, int w, int h) {
        if (barFrame == null) {
            // Fallback: simple border
            g2d.setColor(new Color(0xC8A96E));
            g2d.drawRect(x, y, w, h);
            return;
        }
        // Simple horizontal stretch (the bar asset has no corners to protect)
        g2d.drawImage(barFrame, x, y, w, h, null);
    }

    // ─────────────────────────────────────────────────────────
    // HUD OVERLAY
    // ─────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2d, int W) {
        // Top bar: semi-transparent dark strip
        g2d.setColor(new Color(0, 0, 0, 140));
        g2d.fillRect(0, 0, W, 26);

        // Wave + turn info
        g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2d.setColor(new Color(0xF5E6C8));
        g2d.drawString("⚡  WAVE " + turnManager.getCurrentWave(), 14, 18);

        String stateStr;
        if (turnManager.getCurrentState() == GameState.HERO_ACTION) {
            stateStr = "▶  HERO TURN";
        } else if (turnManager.getCurrentState() == GameState.MONSTER_ACTION) {
            stateStr = "▶  MONSTER TURN";
        } else if (turnManager.getCurrentState() == GameState.REWARD_PHASE) {
            stateStr = "★  CHOOSE REWARD";
        } else if (turnManager.getCurrentState() == GameState.GAME_OVER) {
            stateStr = "☠  GAME OVER";
        } else {
            stateStr = "▶  " + turnManager.getCurrentState().toString();
        }
        
        g2d.setColor(new Color(0xFFD700));
        g2d.drawString(stateStr, W / 2 - 110, 18);

        // Removed VS divider line and text per user request
    }

    // ─────────────────────────────────────────────────────────
    // TURN ORDER UI (Honkai Star Rail style)
    // ─────────────────────────────────────────────────────────
    private void drawTurnOrder(Graphics2D g2d, int W, int H) {
        List<Entity> upcoming = turnManager.getUpcomingTurns(5);
        if (upcoming.isEmpty()) return;

        int slotSize = 40;
        int startX = 40; // Shifted right
        int startY = 60; // Just below the top HUD bar
        
        // Vẽ khung nền mờ bọc danh sách lượt đánh
        int panelWidth = slotSize + 20; // 60
        int panelHeight = upcoming.size() * (slotSize + 10) + 10;
        g2d.setColor(new Color(0, 0, 0, 120));
        g2d.fillRoundRect(startX - 10, startY - 10, panelWidth, panelHeight, 10, 10);
        
        for (int i = 0; i < upcoming.size(); i++) {
            Entity e = upcoming.get(i);
            int y = startY + i * (slotSize + 10);
            
            // Draw slot background
            if (slotImg != null) {
                g2d.drawImage(slotImg, startX, y, slotSize, slotSize, null);
            } else {
                g2d.setColor(new Color(60, 30, 10, 200));
                g2d.fillRect(startX, y, slotSize, slotSize);
            }
            
            // Draw faction tint (Blue for Hero, Red for Monster)
            if (e instanceof Hero) {
                g2d.setColor(new Color(0, 100, 255, 60)); 
            } else {
                g2d.setColor(new Color(255, 0, 0, 60));   
            }
            g2d.fillRect(startX + 2, y + 2, slotSize - 4, slotSize - 4);
            
            // Draw full avatar (static frame 0)
            BufferedImage avatar = (e instanceof Hero) ? heroKnight.getFrame(0) : monsterDemon.getFrame(0);
            if (avatar != null) {
                int pad = 4;
                g2d.drawImage(avatar, 
                    startX + pad, y + pad, slotSize - pad*2, slotSize - pad*2, null);
            }
            
            // Highlight current turn
            if (i == 0) {
                g2d.setColor(Constants.COLOR_HP_YELLOW);
                g2d.drawRect(startX, y, slotSize, slotSize);
                g2d.drawRect(startX - 1, y - 1, slotSize + 2, slotSize + 2); // Thicker border
            }
        }
    }

    // ─────────────────────────────────────────────────────────
    // TOOLTIPS & POINTERS
    // ─────────────────────────────────────────────────────────
    private void drawPointersAndTooltips(Graphics2D g2d, int W, int H) {
        // Vẽ mũi tên lơ lửng trên đầu Hero đang tới lượt
        Entity currentActor = turnManager.getCurrentActor();
        if (currentActor != null && currentActor instanceof Hero && !currentActor.isDead()) {
            HitboxRecord record = getHitboxRecord(currentActor);
            if (record != null && pointImg != null) {
                int px = record.spriteX + (record.spriteW - pointImg.getWidth()) / 2;
                int py = record.spriteY - pointImg.getHeight() - 35; // Dùng chiều cao ảnh để đẩy lên hẳn tên
                g2d.drawImage(pointImg, px, py, null);
            }
        }

        // Nếu có chuột đang trỏ vào Monster
        if (hoveredEntity != null && hoveredEntity instanceof Monster && !hoveredEntity.isDead()) {
            HitboxRecord record = getHitboxRecord(hoveredEntity);
            if (record != null) {
                if (turnManager.getCurrentState() == GameState.SELECT_TARGET) {
                    // Trạng thái ngắm bắn -> Vẽ Point lơ lửng (tĩnh, không nảy)
                    if (pointImg != null) {
                        int px = record.spriteX + (record.spriteW - pointImg.getWidth()) / 2;
                        int py = record.spriteY - pointImg.getHeight() - 35;
                        g2d.drawImage(pointImg, px, py, null);
                    }
                } else {
                    // Trạng thái khác -> Hiển thị Tooltip Info góc PHẢI trên
                    if (descFrameImg != null) {
                        int tooltipW = 200;
                        int tooltipH = 100;
                        int tooltipX = W - tooltipW - 20;
                        int tooltipY = 40; // Bên dưới thanh Wave Info
                        
                        g2d.drawImage(descFrameImg, tooltipX, tooltipY, tooltipW, tooltipH, null);
                        
                        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
                        g2d.setColor(Color.BLACK); // Đổi màu text thành Đen
                        g2d.drawString("Enemy: " + hoveredEntity.getName(), tooltipX + 15, tooltipY + 25);
                        
                        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
                        g2d.drawString("HP: " + hoveredEntity.getCurrentHp() + "/" + hoveredEntity.getStats().getMaxHp(), tooltipX + 15, tooltipY + 45);
                        g2d.drawString("ATK: " + hoveredEntity.getStats().getAttack(), tooltipX + 15, tooltipY + 60);
                        g2d.drawString("DEF: " + hoveredEntity.getStats().getDefense(), tooltipX + 15, tooltipY + 75);
                        g2d.drawString("SPD: " + hoveredEntity.getStats().getSpeed(), tooltipX + 15, tooltipY + 90);
                    }
                }
            }
        }
    }
}
