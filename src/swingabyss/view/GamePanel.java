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
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

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
public class GamePanel extends JPanel {

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

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.GAME_HEIGHT));
        setBackground(Color.BLACK);

        loadAssets();
        createAnimators();
        startAllAnimators();
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
        // Uses the new folder loading approach
        monsterDemon = new SpriteAnimator(
                Constants.PATH_MONSTER_DEMON_IDLE, 
                Constants.SPRITE_SCALE,
                this::repaint);
        monsterDemon.setFlipped(true);
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
        drawEntity(g2d, heroKnight,
                100,  groundY - heroKnight.getFrameHeight(),
                hpKnight, "Knight");

        // ── 4. Monster sprites (right side) ─────────────────
        drawEntity(g2d, monsterDemon,
                550, groundY - monsterDemon.getFrameHeight(),
                hpDemon, "Demon");

        // ── 5. HUD overlay ──────────────────────────────────
        drawHUD(g2d, W);
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
                             int x, int y, int[] hp, String name) {
        BufferedImage frame = animator.getCurrentFrame();
        int fw = animator.getFrameWidth();
        int fh = animator.getFrameHeight();

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
        int nameX = x + (fw - fm.stringWidth(name)) / 2;
        g2d.drawString(name, nameX, y - 22);

        // Draw HP bar above sprite
        int barW = Math.max(fw, 60);
        int barX = x + (fw - barW) / 2;
        int barY = y - 14;
        drawHpBar(g2d, barX, barY, barW, hp[0], hp[1]);
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
            // Critical HP: pulse the alpha using sine wave for visual urgency
            fillColor = Constants.COLOR_HP_RED;
            float pulse = 0.6f + 0.4f * (float) Math.sin(tick * 0.15);
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
        g2d.drawString("⚡  WAVE 1", 14, 18);

        String state = "▶  HERO TURN — Wizard's Action";
        g2d.setColor(new Color(0xFFD700));
        g2d.drawString(state, W / 2 - 110, 18);

        // VS divider line in the middle of the arena
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.fillRect(W / 2 - 1, 26, 2, getHeight() - 26);

        // "VS" text at center
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.drawString("VS", W / 2 - 12, getHeight() / 2 + 9);
    }
}
