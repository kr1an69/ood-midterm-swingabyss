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
/*
 * heroes:
 * knight attack 5 frame - idle 7 frame
 * magician attack 7 frame - idle 8 frame
 * swordswoman attack 6 frame - idle 4 frame
 * monsters:
 * beast attack 6 frame - idle 6 frame
 * dragon attack 7 frame - idle 6 frame
 * demon attack 11 frame - idle 6 frame
 * ghost attack 4 frame - idle 7 frame
 * goblin attack 8 frame - idle 4 frame
 * ogre attack 7 frame - idle 4 frame
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
    public static final String UI_DEFAULT_FRAME = "/assets/ui/book/default_frame.png";
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

    // ──────────────────────────────────────────
    // DYNAMIC SPRITE CONFIGS
    // ──────────────────────────────────────────
    public static class SpriteConfig {
        public String path;
        public int frameWidth;
        public int frameHeight;
        public int frameCount;
        public SpriteConfig(String path, int frameWidth, int frameHeight, int frameCount) {
            this.path = path;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
            this.frameCount = frameCount;
        }
    }

    public static SpriteConfig getIdleSpriteConfig(String entityName, boolean isHero) {
        String name = entityName.toLowerCase();
        String path = isHero ? "/assets/heroes/" + name + "/spritesheets/idle_spritesheet.png" 
                             : "/assets/monsters/" + name + "/spritesheets/idle_spritesheet.png";
        
        if (isHero) {
            switch (name) {
                case "knight": return new SpriteConfig(path, 96, 84, 7);
                case "magician": return new SpriteConfig(path, 128, 128, 8);
                case "swordswoman": return new SpriteConfig(path, 64, 80, 4);
            }
        } else {
            switch (name) {
                case "beast": return new SpriteConfig(path, 80, 160, 6);
                case "demon": return new SpriteConfig(path, 160, 144, 6);
                case "dragon": return new SpriteConfig(path, 144, 64, 6);
                case "ghost": return new SpriteConfig(path, 64, 80, 7);
                case "goblin": return new SpriteConfig(path, 150, 150, 4);
                case "ogre": return new SpriteConfig(path, 144, 80, 4);
            }
        }
        return null;
    }

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
