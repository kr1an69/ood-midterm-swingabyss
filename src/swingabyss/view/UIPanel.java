package swingabyss.view;

import swingabyss.utils.Constants;
import swingabyss.utils.SpriteLoader;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * UIPanel — the action bar at the bottom of the game window.
 *
 * Visual design:
 *   - Background: the Book Cover parchment frame rendered via NineSlicePanel.
 *   - Action buttons: each is a custom-painted slot using UI_TravelBook_Slot01a.png
 *     scaled via the 9-slice algorithm for crisp pixel art.
 *   - Text: uses a Monospaced font to maintain pixel-game aesthetic.
 *
 * Architecture:
 *   - Extends NineSlicePanel so the parchment frame is automatically the background.
 *   - Buttons are inner ActionButton panels painted with the slot asset.
 *   - Button click events will eventually dispatch Command objects to the
 *     CommandQueue (Command Pattern integration point — stubbed here).
 */
public class UIPanel extends NineSlicePanel {

    private static final long serialVersionUID = 1L;

    // Action button labels — matches the Command Pattern names
    private static final String[] ACTIONS = { "⚔  Attack", "✨  Skill", "🛡  Defend", "🧪  Item" };

    // Slot image used for each button (Flyweight — loaded once, shared by all buttons)
    private final BufferedImage slotImage;

    public UIPanel() {
        // Use the Book Cover parchment frame for the panel background
        super(Constants.UI_BOOK_COVER, Constants.INSETS_BOOK_COVER);
        setLayout(new BorderLayout(0, 0));

        // Pre-load slot image into Flyweight cache
        slotImage = SpriteLoader.getInstance().loadImage(Constants.UI_SLOT);

        // Title label area (top strip inside the panel)
        JPanel titleRow = buildTitleRow();
        add(titleRow, BorderLayout.NORTH);

        // Button grid (center of panel)
        JPanel buttonRow = buildButtonRow();
        add(buttonRow, BorderLayout.CENTER);
    }

    // ─────────────────────────────────────────────────────────
    // TITLE ROW
    // ─────────────────────────────────────────────────────────

    private JPanel buildTitleRow() {
        JPanel row = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Wave indicator text — left side
                g2d.setFont(new Font("Monospaced", Font.BOLD, 11));
                g2d.setColor(Constants.COLOR_DARK_BROWN);
                g2d.drawString("⚡ Wave 1 — Hero's Turn", 8, 14);

                // Right side: small status
                String status = "HP: 80/100  MP: 40/50";
                FontMetrics fm = g2d.getFontMetrics();
                int textW = fm.stringWidth(status);
                g2d.drawString(status, getWidth() - textW - 10, 14);
            }
        };
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 20));
        return row;
    }

    // ─────────────────────────────────────────────────────────
    // BUTTON ROW
    // ─────────────────────────────────────────────────────────

    private JPanel buildButtonRow() {
        JPanel row = new JPanel(new GridLayout(1, ACTIONS.length, 12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 14, 8, 14));

        for (int i = 0; i < ACTIONS.length; i++) {
            final int idx = i;
            row.add(new ActionButton(ACTIONS[i], slotImage, () -> {
                // TODO: Dispatch Command object to CommandQueue
                // Example: commandQueue.enqueue(new AttackCommand(activeHero, selectedTarget));
                System.out.println("[UIPanel] Action triggered: " + ACTIONS[idx]);
            }));
        }
        return row;
    }

    // ─────────────────────────────────────────────────────────
    // INNER CLASS: ActionButton
    // ─────────────────────────────────────────────────────────

    /**
     * A custom action button drawn using the Slot UI asset as background.
     * Paints itself using Graphics2D instead of relying on JButton's LAF,
     * so the parchment/book style is fully preserved.
     */
    private static class ActionButton extends JPanel {

        private static final long serialVersionUID = 1L;

        // Slot 9-slice boundaries (4px border in the ~20x20 slot image)
        private static final Insets SLOT_INSETS = new Insets(4, 4, 4, 4);

        private final String label;
        private final BufferedImage slotImg;
        private final Runnable action;

        // Hover state for visual feedback micro-animation
        private boolean hovered = false;
        private boolean pressed = false;

        ActionButton(String label, BufferedImage slotImg, Runnable action) {
            this.label  = label;
            this.slotImg = slotImg;
            this.action = action;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(120, 60));

            // Mouse interaction — will become Command dispatchers later
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true; repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false; pressed = false; repaint();
                }
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    pressed = true; repaint();
                }
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (hovered) { pressed = false; action.run(); }
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Offset when pressed — simulates button press depth
            int ox = pressed ? 1 : 0;
            int oy = pressed ? 2 : 0;

            // Draw slot background via 9-slice
            drawSlot(g2d, ox, oy, getWidth() - ox, getHeight() - oy);

            // Hover glow overlay
            if (hovered && !pressed) {
                g2d.setColor(new Color(255, 240, 180, 50));
                g2d.fillRoundRect(ox + 2, oy + 2, getWidth() - ox - 4, getHeight() - oy - 4, 4, 4);
            }

            // Label text
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = new Font("Monospaced", Font.BOLD, 12);
            g2d.setFont(font);
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(label)) / 2 + ox;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + oy;

            // Text shadow for depth
            g2d.setColor(Constants.COLOR_DARK_BROWN.darker());
            g2d.drawString(label, textX + 1, textY + 1);
            // Main text: parchment gold
            g2d.setColor(new Color(0xF5E6C8));
            g2d.drawString(label, textX, textY);
        }

        /** Draws the slot image via a simple 9-slice stretch. */
        private void drawSlot(Graphics2D g2d, int dx, int dy, int dw, int dh) {
            if (slotImg == null) {
                // Fallback solid button
                g2d.setColor(new Color(60, 30, 10, 200));
                g2d.fillRoundRect(dx, dy, dw, dh, 6, 6);
                g2d.setColor(new Color(0xC8A96E));
                g2d.drawRoundRect(dx, dy, dw - 1, dh - 1, 6, 6);
                return;
            }

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            int sw = slotImg.getWidth(), sh = slotImg.getHeight();
            int sL = SLOT_INSETS.left,   sT = SLOT_INSETS.top;
            int sR = sw - SLOT_INSETS.right, sB = sh - SLOT_INSETS.bottom;
            int dL = sL, dT = sT, dR = dw - SLOT_INSETS.right, dB = dh - SLOT_INSETS.bottom;

            if (dR <= dL || dB <= dT) { g2d.drawImage(slotImg, dx, dy, dw, dh, null); return; }

            // 9 regions (same algorithm as NineSlicePanel)
            drawR(g2d, slotImg, dx,    dy,    dL,    dT,    0,  0,  sL,    sT   );
            drawR(g2d, slotImg, dx+dL, dy,    dR-dL, dT,    sL, 0,  sR-sL, sT   );
            drawR(g2d, slotImg, dx+dR, dy,    dw-dR, dT,    sR, 0,  sw-sR, sT   );
            drawR(g2d, slotImg, dx,    dy+dT, dL,    dB-dT, 0,  sT, sL,    sB-sT);
            drawR(g2d, slotImg, dx+dL, dy+dT, dR-dL, dB-dT, sL, sT, sR-sL, sB-sT);
            drawR(g2d, slotImg, dx+dR, dy+dT, dw-dR, dB-dT, sR, sT, sw-sR, sB-sT);
            drawR(g2d, slotImg, dx,    dy+dB, dL,    dh-dB, 0,  sB, sL,    sh-sB);
            drawR(g2d, slotImg, dx+dL, dy+dB, dR-dL, dh-dB, sL, sB, sR-sL, sh-sB);
            drawR(g2d, slotImg, dx+dR, dy+dB, dw-dR, dh-dB, sR, sB, sw-sR, sh-sB);
        }

        private void drawR(Graphics2D g, BufferedImage img,
                           int dx, int dy, int dw, int dh,
                           int sx, int sy, int sw, int sh) {
            if (dw <= 0 || dh <= 0 || sw <= 0 || sh <= 0) return;
            g.drawImage(img, dx, dy, dx+dw, dy+dh, sx, sy, sx+sw, sy+sh, null);
        }
    }
}
