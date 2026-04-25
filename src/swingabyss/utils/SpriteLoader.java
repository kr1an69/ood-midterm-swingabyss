package swingabyss.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * SpriteLoader — Singleton + Flyweight cache for all game images.
 *
 * Design Patterns:
 * ┌─────────────────────────────────────────────────────────┐
 * │  SINGLETON: Only one SpriteLoader instance ever exists. │
 * │  FLYWEIGHT: Images loaded from disk are cached by path. │
 * │             Subsequent requests reuse the same object   │
 * │             reference — no re-reading from disk.        │
 * └─────────────────────────────────────────────────────────┘
 *
 * Key algorithm — Nearest-Neighbor Scaling:
 *   Java's default getScaledInstance() uses bilinear interpolation,
 *   which blurs pixel art. We use AffineTransformOp with
 *   TYPE_NEAREST_NEIGHBOR to keep each pixel sharp when
 *   scaling up spritesheets by SPRITE_SCALE factor.
 */
public class SpriteLoader {

    // ── Singleton instance ───────────────────────────────────
    private static SpriteLoader instance;

    // ── Flyweight cache: path → image ────────────────────────
    private final Map<String, BufferedImage> cache = new HashMap<>();

    /** Private constructor enforces Singleton pattern. */
    private SpriteLoader() {}

    /**
     * Returns the sole SpriteLoader instance (lazy initialization).
     * Not synchronized — single-threaded Swing EDT usage is safe here.
     */
    public static SpriteLoader getInstance() {
        if (instance == null) {
            instance = new SpriteLoader();
        }
        return instance;
    }

    // ─────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────

    /**
     * Loads (or retrieves from cache) a BufferedImage at the given classpath path.
     * Path should start with "/" and be relative to the project root,
     * e.g. "/assets/sprites/wizard.png".
     *
     * @param path classpath-relative resource path
     * @return the loaded image, or a 1×1 placeholder on failure
     */
    public BufferedImage loadImage(String path) {
        // Flyweight: return cached reference if already loaded
        if (cache.containsKey(path)) {
            return cache.get(path);
        }

        BufferedImage img = null;
        try {
            // Strategy 1: Try classpath resource (works in Maven/packaged JAR)
            InputStream stream = getClass().getResourceAsStream(path);

            if (stream == null) {
                // Strategy 2: Fall back to file system relative to working directory.
                // In Eclipse projects, assets/ lives at the project root (the working dir),
                // so path "/assets/sprites/wizard.png" maps to "./assets/sprites/wizard.png".
                String filePath = path.startsWith("/") ? path.substring(1) : path;
                File file = new File(filePath);
                if (file.exists()) {
                    img = ImageIO.read(file);
                    System.out.println("[SpriteLoader] Loaded from filesystem: " + filePath);
                } else {
                    System.err.println("[SpriteLoader] Not found anywhere: " + path);
                    img = makePlaceholder(32, 32);
                }
            } else {
                img = ImageIO.read(stream);
                System.out.println("[SpriteLoader] Loaded from classpath: " + path);
            }
        } catch (IOException e) {
            System.err.println("[SpriteLoader] Failed to load: " + path + " — " + e.getMessage());
            img = makePlaceholder(32, 32);
        }

        cache.put(path, img);  // Cache for Flyweight reuse
        return img;
    }

    /**
     * Extracts a sub-image (single frame) from a spritesheet.
     * The result is NOT cached separately — callers hold their own reference.
     *
     * @param sheet    the full spritesheet BufferedImage
     * @param x        left edge of the frame in pixels
     * @param y        top edge of the frame in pixels
     * @param w        frame width in pixels
     * @param h        frame height in pixels
     * @return         a new BufferedImage representing a single frame
     */
    public BufferedImage getSubImage(BufferedImage sheet, int x, int y, int w, int h) {
        // Clamp to prevent ArrayIndexOutOfBounds on mis-configured frames
        int safeX = Math.min(x, sheet.getWidth()  - 1);
        int safeY = Math.min(y, sheet.getHeight() - 1);
        int safeW = Math.min(w, sheet.getWidth()  - safeX);
        int safeH = Math.min(h, sheet.getHeight() - safeY);
        return sheet.getSubimage(safeX, safeY, safeW, safeH);
    }

    /**
     * Scales a BufferedImage using the Nearest-Neighbor algorithm.
     *
     * Why Nearest-Neighbor?
     *   Pixel art consists of deliberate, hard-edged pixels.
     *   Bilinear/bicubic interpolation blends neighbouring pixels,
     *   producing a blurry result at 2× or 3× scale.
     *   Nearest-Neighbor maps each output pixel to the closest input
     *   pixel with no blending — preserving the crisp pixel aesthetic.
     *
     * @param source  original (small) image
     * @param scale   integer scale factor (e.g. 2 for 2×)
     * @return        new scaled image, same type as source
     */
    public BufferedImage getScaledPixel(BufferedImage source, int scale) {
        int newW = source.getWidth()  * scale;
        int newH = source.getHeight() * scale;

        BufferedImage scaled = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        // Build an AffineTransform that scales uniformly
        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);

        // TYPE_NEAREST_NEIGHBOR: no interpolation — pure pixel mapping
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        op.filter(source, scaled);

        return scaled;
    }

    /**
     * Convenience: load a full spritesheet and immediately cut all frames
     * into an array, applying Nearest-Neighbor scaling.
     *
     * @param path        classpath path to the spritesheet
     * @param frameW      width of each frame (un-scaled pixels)
     * @param frameH      height of each frame (un-scaled pixels)
     * @param totalFrames number of frames to extract (horizontal strip)
     * @param scale       integer scale factor for output frames
     * @return            array of scaled frame images
     */
    public BufferedImage[] loadFrames(String path, int frameW, int frameH,
                                      int totalFrames, int scale) {
        BufferedImage sheet = loadImage(path);
        BufferedImage[] frames = new BufferedImage[totalFrames];
        for (int i = 0; i < totalFrames; i++) {
            BufferedImage raw = getSubImage(sheet, i * frameW, 0, frameW, frameH);
            frames[i] = (scale > 1) ? getScaledPixel(raw, scale) : raw;
        }
        return frames;
    }

    /**
     * Loads multiple frames from a directory (each frame is a separate .png file).
     * Files are sorted intelligently (e.g. idle2.png before idle10.png).
     */
    public BufferedImage[] loadFramesFromFolder(String folderPath, int scale) {
        String filePath = folderPath.startsWith("/") ? folderPath.substring(1) : folderPath;
        File folder = new File(filePath);
        
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("[SpriteLoader] Folder not found: " + folderPath);
            return new BufferedImage[] { makePlaceholder(32, 32) };
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            System.err.println("[SpriteLoader] No .png files found in folder: " + folderPath);
            return new BufferedImage[] { makePlaceholder(32, 32) };
        }

        // Custom comparator to sort files like frame1.png, frame2.png, frame10.png correctly
        java.util.Arrays.sort(files, (f1, f2) -> {
            String n1 = f1.getName().replaceAll("\\D", ""); // Extract numbers
            String n2 = f2.getName().replaceAll("\\D", "");
            int i1 = n1.isEmpty() ? 0 : Integer.parseInt(n1);
            int i2 = n2.isEmpty() ? 0 : Integer.parseInt(n2);
            if (i1 == i2) {
                return f1.getName().compareTo(f2.getName());
            }
            return Integer.compare(i1, i2);
        });

        BufferedImage[] sortedFrames = new BufferedImage[files.length];
        for (int i = 0; i < files.length; i++) {
            String virtualPath = folderPath + (folderPath.endsWith("/") ? "" : "/") + files[i].getName();
            BufferedImage raw = loadImage(virtualPath);
            sortedFrames[i] = (scale > 1) ? getScaledPixel(raw, scale) : raw;
        }

        return sortedFrames;
    }

    // ─────────────────────────────────────────────────────────
    // INTERNAL HELPERS
    // ─────────────────────────────────────────────────────────

    /**
     * Creates a magenta/black checkerboard placeholder image.
     * Used when an asset file is missing — visually obvious for debugging.
     */
    private BufferedImage makePlaceholder(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        for (int py = 0; py < h; py += 8) {
            for (int px = 0; px < w; px += 8) {
                g.setColor(((px / 8 + py / 8) % 2 == 0)
                        ? new java.awt.Color(0xFF00FF)
                        : java.awt.Color.BLACK);
                g.fillRect(px, py, 8, 8);
            }
        }
        g.dispose();
        return img;
    }
}
