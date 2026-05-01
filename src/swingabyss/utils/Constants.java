package swingabyss.utils;

import java.awt.Insets;

/**
 * Centralized configuration constants for Swing Into The Abyss.
 * 
 * Acts as the single source of truth for:
 * - Window geometry
 * - Asset paths (relative to the /assets directory at project root)
 * - Sprite frame dimensions per character
 * - 9-Slice insets for each UI element
 * - Rendering & animation timing
 */
public final class Constants {

    // ──────────────────────────────────────────
    // WINDOW
    // ──────────────────────────────────────────
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final int GAME_HEIGHT = 580; // GamePanel height
    public static final int UI_HEIGHT = 140; // UIPanel height

    // ──────────────────────────────────────────
    // ASSET PATHS (relative to assets/ directory at project root)
    // ──────────────────────────────────────────

    // UI – Book Style
    public static final String UI_BOOK_COVER = "/assets/ui/book/default_frame.png";
    public static final String UI_BOOK_PAGE_LEFT = "/assets/ui/book/book_page_left.png";
    public static final String UI_SLOT = "/assets/ui/book/slot.png";
    public static final String UI_BAR_FRAME = "/assets/ui/book/bar_frame.png";
    public static final String UI_POINT = "/assets/ui/book/point.png";
    public static final String UI_BAR_FILL = "/assets/ui/book/fill.png";
    public static final String UI_BAR_FILL_RED = "/assets/ui/book/fill_red.png";
    public static final String UI_DESCRIPTION_FRAME = "/assets/ui/book/description_frame.png";
    public static final String UI_PAUSE = "/assets/ui/book/pause.png";
    public static final String UI_REWARD_CARD = "/assets/ui/book/reward_card.png";
    public static final String UI_REWARD_CARD_SHADOW = "/assets/ui/book/reward_card_shadow.png";
    // Backgrounds (Gothic Horror — 3 parallax layers)
    public static final String BG_CLOUDS = "/assets/bg/bg_clouds.png";
    public static final String BG_TOWN = "/assets/bg/bg_town.png";
    public static final String BG_TILES = "/assets/bg/bg_tiles.png";

    // Character & Monster Paths (Giai đoạn 3)
    public static final String PATH_HERO_KNIGHT_IDLE = "/assets/heroes/knight/spritesheets/idle_spritesheet.png";
    public static final String PATH_MONSTER_DEMON_IDLE = "/assets/monsters/demon/sprites/idle";

    // ──────────────────────────────────────────
    // SPRITE FRAME CONFIGS
    // ──────────────────────────────────────────
    // Knight idle spritesheet uses 1 sheet: 672x84 -> 7 frames of 96x84
    public static final int[] KNIGHT_IDLE_FRAMES = { 96, 84, 7 };

    // ──────────────────────────────────────────
    // REWARD CARD CONFIGS
    // ──────────────────────────────────────────
    // Actual card content within the 420x420 transparent image (x, y, width,
    // height)
    public static final int[] REWARD_CARD_CROP = { 66, 39, 288, 342 };
    public static final int[] REWARD_SHADOW_CROP = { 66, 39, 302, 355 };

    // ──────────────────────────────────────────
    // DISPLAY SCALE (Nearest-Neighbor scale factor)
    // Sprites from Legacy Collection are already large pixel art (~80-150px tall).
    // Use scale=1 for natural size; 2 would make them ~200-300px (too big).
    // ──────────────────────────────────────────
    public static final int SPRITE_SCALE = 1;

    // ──────────────────────────────────────────
    // ANIMATION TIMING
    // ──────────────────────────────────────────
    /** Milliseconds between sprite frames */
    public static final int ANIM_DELAY_MS = 180;

    // ──────────────────────────────────────────
    // 9-SLICE INSETS (top, left, bottom, right pixels in the SOURCE image)
    // Measured from actual asset dimensions:
    // book_cover: 224x160 — gold corner decorations ~12px each edge
    // slot: 30x30 — dark border ~5px each edge
    // bar_frame: 62x4 — thin horizontal strip, no meaningful corners
    // ──────────────────────────────────────────
    /** Insets for the Book Cover frame used in UIPanel */
    public static final Insets INSETS_BOOK_COVER = new Insets(12, 12, 12, 12);
    /** Insets for the slot button images */
    public static final Insets INSETS_SLOT = new Insets(5, 5, 5, 5);
    /**
     * Insets for the HP bar frame (thin horizontal strip — treat as stretch-all)
     */
    public static final Insets INSETS_BAR = new Insets(1, 2, 1, 2);
    /** Insets for the description frame (tooltip/info box) */
    public static final Insets INSETS_DESCRIPTION_FRAME = new Insets(8, 8, 8, 8);
    // ──────────────────────────────────────────
    // COLORS (fallback palette matching the parchment theme)
    // ──────────────────────────────────────────
    public static final java.awt.Color COLOR_PARCHMENT = new java.awt.Color(0xF5DEB3); // wheat
    public static final java.awt.Color COLOR_DARK_BROWN = new java.awt.Color(0x3B1A0A);
    public static final java.awt.Color COLOR_HP_GREEN = new java.awt.Color(0x4CAF50);
    public static final java.awt.Color COLOR_HP_YELLOW = new java.awt.Color(0xFFC107);
    public static final java.awt.Color COLOR_HP_RED = new java.awt.Color(0xF44336);

    // Private constructor – utility class, never instantiated
    private Constants() {
    }
}
