package swingabyss.view;

import swingabyss.utils.SpriteLoader;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * NineSlicePanel — a JPanel that paints its background using the 9-Slice algorithm.
 *
 * ═══════════════════════════════════════════════════════════════════
 * THE 9-SLICE ALGORITHM EXPLAINED
 * ═══════════════════════════════════════════════════════════════════
 *
 * The problem with naive image scaling: if you stretch a bordered UI frame
 * uniformly, the corners get distorted.
 *
 * The 9-slice technique divides the source image into 9 fixed regions:
 *
 *   ┌──────┬───────────────┬──────┐   ← top = insets.top pixels
 *   │  TL  │   Top Edge    │  TR  │
 *   ├──────┼───────────────┼──────┤
 *   │ Left │    Center     │Right │   ← stretches to fill any size
 *   │ Edge │               │ Edge │
 *   ├──────┼───────────────┼──────┤
 *   │  BL  │ Bottom Edge   │  BR  │
 *   └──────┴───────────────┴──────┘   ← bottom = insets.bottom pixels
 *      ↑                       ↑
 *   left = insets.left       right = insets.right
 *
 * Rules:
 *   - 4 CORNERS: never scaled — drawn at their original size.
 *   - TOP/BOTTOM EDGES: stretched horizontally only.
 *   - LEFT/RIGHT EDGES: stretched vertically only.
 *   - CENTER: stretched in both directions.
 *
 * This preserves crisp corners at any panel size.
 * ═══════════════════════════════════════════════════════════════════
 */
public class NineSlicePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    // The source image from which the 9 slices are derived
    private BufferedImage sourceImage;

    // Insets define the slice boundaries (pixels from each edge in the SOURCE image)
    private Insets sliceInsets;

    // ─────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────

    /**
     * Full constructor: loads the given image and uses the provided insets
     * to define the 9-slice boundaries.
     *
     * @param imagePath    classpath path to the UI image (e.g. "/assets/ui/book/book_cover.png")
     * @param sliceInsets  top/left/bottom/right pixel distances from edge of the source image
     */
    public NineSlicePanel(String imagePath, Insets sliceInsets) {
        super();
        this.setOpaque(false);
        this.sliceInsets = sliceInsets;
        this.sourceImage = SpriteLoader.getInstance().loadImage(imagePath);
    }

    /**
     * Default fallback constructor — draws a plain parchment-colored background.
     * Used when no asset path is specified (e.g. during early prototyping).
     */
    public NineSlicePanel() {
        super();
        this.setOpaque(false);
        this.sourceImage = null;
        this.sliceInsets = new Insets(8, 8, 8, 8);
    }

    // ─────────────────────────────────────────────────────────
    // PAINT
    // ─────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (sourceImage == null) {
            // Fallback: draw a parchment-coloured rounded rect
            drawFallback(g2d);
        } else {
            // Draw the actual 9-slice image
            drawNineSlice(g2d, 0, 0, getWidth(), getHeight());
        }
    }

    // ─────────────────────────────────────────────────────────
    // 9-SLICE CORE ALGORITHM
    // ─────────────────────────────────────────────────────────

    /**
     * Draws the 9 slices of sourceImage to fill the rectangle (dx, dy, dw, dh).
     *
     * Variable naming convention:
     *   sx_ = source x coordinates    sy_ = source y coordinates
     *   dx_ = destination x coords    dy_ = destination y coords
     */
    private void drawNineSlice(Graphics2D g2d, int dx, int dy, int dw, int dh) {
        // Disable interpolation — use nearest-neighbor for pixel-crisp UI
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        final int sw = sourceImage.getWidth();
        final int sh = sourceImage.getHeight();

        // Source slice boundaries
        final int sL = sliceInsets.left;
        final int sT = sliceInsets.top;
        final int sR = sw - sliceInsets.right;
        final int sB = sh - sliceInsets.bottom;

        // Destination slice boundaries
        final int dL = sliceInsets.left;
        final int dT = sliceInsets.top;
        final int dR = dw - sliceInsets.right;
        final int dB = dh - sliceInsets.bottom;

        // Guard: if the panel is smaller than the insets, just tile the whole image
        if (dR <= dL || dB <= dT) {
            g2d.drawImage(sourceImage, dx, dy, dw, dh, null);
            return;
        }

        // ── Draw all 9 regions ──────────────────────────────
        // TOP-LEFT corner
        drawRegion(g2d, dx,      dy,      dL,      dT,
                        0,       0,       sL,      sT);
        // TOP edge  (stretch horizontally)
        drawRegion(g2d, dx+dL,   dy,      dR-dL,   dT,
                        sL,      0,       sR-sL,   sT);
        // TOP-RIGHT corner
        drawRegion(g2d, dx+dR,   dy,      dw-dR,   dT,
                        sR,      0,       sw-sR,   sT);

        // LEFT edge  (stretch vertically)
        drawRegion(g2d, dx,      dy+dT,   dL,      dB-dT,
                        0,       sT,      sL,      sB-sT);
        // CENTER  (stretch both directions)
        drawRegion(g2d, dx+dL,   dy+dT,   dR-dL,   dB-dT,
                        sL,      sT,      sR-sL,   sB-sT);
        // RIGHT edge  (stretch vertically)
        drawRegion(g2d, dx+dR,   dy+dT,   dw-dR,   dB-dT,
                        sR,      sT,      sw-sR,   sB-sT);

        // BOTTOM-LEFT corner
        drawRegion(g2d, dx,      dy+dB,   dL,      dh-dB,
                        0,       sB,      sL,      sh-sB);
        // BOTTOM edge  (stretch horizontally)
        drawRegion(g2d, dx+dL,   dy+dB,   dR-dL,   dh-dB,
                        sL,      sB,      sR-sL,   sh-sB);
        // BOTTOM-RIGHT corner
        drawRegion(g2d, dx+dR,   dy+dB,   dw-dR,   dh-dB,
                        sR,      sB,      sw-sR,   sh-sB);
    }

    /**
     * Draws a single region from the source image onto the destination.
     * Both source and destination rectangles are specified as (x, y, width, height).
     * Skips drawing if either rectangle has zero area.
     */
    private void drawRegion(Graphics2D g2d,
                             int dx, int dy, int dw, int dh,
                             int sx, int sy, int sw, int sh) {
        if (dw <= 0 || dh <= 0 || sw <= 0 || sh <= 0) return;

        // drawImage(img, dx1,dy1,dx2,dy2, sx1,sy1,sx2,sy2, observer)
        g2d.drawImage(sourceImage,
                dx,      dy,      dx + dw, dy + dh,
                sx,      sy,      sx + sw, sy + sh,
                null);
    }

    // ─────────────────────────────────────────────────────────
    // FALLBACK RENDERING  (no asset loaded)
    // ─────────────────────────────────────────────────────────

    private void drawFallback(Graphics2D g2d) {
        // Dark semi-transparent background
        g2d.setColor(new java.awt.Color(40, 40, 45, 220));
        g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);

        // Parchment-tinted border
        g2d.setColor(new java.awt.Color(0xC8A96E));
        g2d.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 12, 12);
    }
}
