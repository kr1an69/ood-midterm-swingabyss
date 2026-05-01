package swingabyss.view;

import swingabyss.utils.Constants;
import swingabyss.utils.SpriteLoader;

import javax.swing.Timer;
import java.awt.image.BufferedImage;

/**
 * SpriteAnimator — a self-contained animation component.
 *
 * Responsibilities:
 * - Holds a pre-cut array of frames (BufferedImage[]).
 * - Uses a javax.swing.Timer to advance frames at a fixed interval.
 * - Notifies a Runnable callback (typically panel.repaint()) on each tick.
 *
 * Design notes:
 * - Frames are stored as references to sub-images of the shared Flyweight
 *   spritesheet — no pixel data is duplicated.
 * - The Timer fires on the Event Dispatch Thread (EDT), so calling repaint()
 *   from the callback is safe without explicit synchronization.
 * - Pause / resume support lets the TurnManager freeze animations when the
 *   game is waiting for player input (future integration point).
 */
public class SpriteAnimator {

    private final BufferedImage[] frames;
    private int currentFrame = 0;
    private final Timer timer;
    private boolean flipped = false;  // Mirror sprite for left-facing direction

    /**
     * Creates a SpriteAnimator that loads its frames from a spritesheet.
     *
     * @param sheetPath   classpath path to the spritesheet (e.g. "/assets/sprites/wizard.png")
     * @param frameW      width of each frame in the source image (un-scaled)
     * @param frameH      height of each frame in the source image (un-scaled)
     * @param totalFrames number of horizontal frames to use
     * @param scale       Nearest-Neighbor scale factor (typically Constants.SPRITE_SCALE)
     * @param onTick      Runnable called every animation tick — use () -> panel.repaint()
     */
    public SpriteAnimator(String sheetPath, int frameW, int frameH,
                          int totalFrames, int scale, Runnable onTick) {
        // Load and cut frames via SpriteLoader (Flyweight for the sheet itself)
        this.frames = SpriteLoader.getInstance()
                .loadFrames(sheetPath, frameW, frameH, totalFrames, scale);

        // Swing Timer fires on the EDT — safe for UI updates
        this.timer = new Timer(Constants.ANIM_DELAY_MS, e -> {
            currentFrame = (currentFrame + 1) % frames.length;
            if (onTick != null) onTick.run();
        });
    }

    /**
     * Creates a SpriteAnimator that loads its frames from a directory of separate images.
     *
     * @param folderPath  classpath path to the folder containing frames (e.g. "/assets/monsters/demon/sprites/idle")
     * @param scale       Nearest-Neighbor scale factor
     * @param onTick      Runnable called every animation tick
     */
    public SpriteAnimator(String folderPath, int scale, Runnable onTick) {
        // Load frames from individual files in the folder via SpriteLoader
        this.frames = SpriteLoader.getInstance()
                .loadFramesFromFolder(folderPath, scale);

        this.timer = new Timer(Constants.ANIM_DELAY_MS, e -> {
            if (frames.length > 0) {
                currentFrame = (currentFrame + 1) % frames.length;
                if (onTick != null) onTick.run();
            }
        });
    }

    /** Starts (or restarts) the animation loop. */
    public void start() {
        if (!timer.isRunning()) timer.start();
    }

    /** Pauses the animation (current frame is preserved). */
    public void pause() {
        timer.stop();
    }

    /** Resumes a paused animation. */
    public void resume() {
        if (!timer.isRunning()) timer.start();
    }

    /** Stops animation and resets to frame 0. */
    public void stop() {
        timer.stop();
        currentFrame = 0;
    }

    /**
     * Sets whether the sprite should be drawn horizontally mirrored.
     * Heroes face right (flipped=false); can be set to true for monsters.
     */
    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    public boolean isFlipped() {
        return flipped;
    }

    /**
     * Returns the current frame image.
     * Call this inside paintComponent() to get the frame to draw.
     */
    public BufferedImage getCurrentFrame() {
        return frames[currentFrame];
    }
    
    /**
     * Returns a specific static frame image (e.g. 0 for idle).
     */
    public BufferedImage getFrame(int index) {
        if (frames == null || frames.length == 0) return null;
        if (index >= 0 && index < frames.length) return frames[index];
        return frames[0];
    }

    /**
     * Returns the scaled frame width (useful for centering the sprite).
     */
    public int getFrameWidth() {
        return frames.length > 0 ? frames[0].getWidth() : 0;
    }

    /**
     * Returns the scaled frame height (useful for positioning the sprite).
     */
    public int getFrameHeight() {
        return frames.length > 0 ? frames[0].getHeight() : 0;
    }
}
