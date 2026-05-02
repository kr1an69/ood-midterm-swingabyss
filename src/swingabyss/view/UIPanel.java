package swingabyss.view;

import swingabyss.utils.Constants;
import swingabyss.utils.SpriteLoader;

import swingabyss.manager.TurnManager;
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
 * - Background: the Book Cover parchment frame rendered via NineSlicePanel.
 * - Action buttons: each is a custom-painted slot using
 * UI_TravelBook_Slot01a.png
 * scaled via the 9-slice algorithm for crisp pixel art.
 * - Text: uses a Monospaced font to maintain pixel-game aesthetic.
 *
 * Architecture:
 * - Extends NineSlicePanel so the parchment frame is automatically the
 * background.
 * - Buttons are inner ActionButton panels painted with the slot asset.
 * - Button click events will eventually dispatch Command objects to the
 * CommandQueue (Command Pattern integration point — stubbed here).
 */
public class UIPanel extends NineSlicePanel {

    private static final long serialVersionUID = 1L;

    // Action button labels — matches the Command Pattern names
    private static final String[] ACTIONS = { "⚔  Attack", "🛡  Defend", "🧪  Heal" };

    // Slot image used for each button (Flyweight — loaded once, shared by all
    // buttons)
    private final BufferedImage slotImage;
    private TurnManager turnManager;

    public UIPanel(TurnManager turnManager) {
        // Use the Book Cover parchment frame for the panel background
        super(Constants.UI_DEFAULT_FRAME, Constants.INSETS_BOOK_COVER);
        this.turnManager = turnManager;
        setLayout(new BorderLayout(0, 0));

        // Pre-load slot image into Flyweight cache
        slotImage = SpriteLoader.getInstance().loadImage(Constants.UI_SLOT);

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.EAST);
    }

    // ─────────────────────────────────────────────────────────
    // LEFT PANEL (Avatar + Stats)
    // ─────────────────────────────────────────────────────────
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                swingabyss.model.Entity actor = turnManager.getCurrentActor();
                if (actor == null)
                    return;

                // Draw Avatar Slot
                int slotX = 20;
                int slotY = 15;
                int slotSize = 55;

                if (slotImage != null) {
                    g2d.drawImage(slotImage, slotX, slotY, slotSize, slotSize, null);
                }

                // Load static frame 0 of Knight for UI Avatar placeholder
                BufferedImage avatar = null;
                if (actor instanceof swingabyss.model.Hero) {
                    BufferedImage sheet = SpriteLoader.getInstance().loadImage(Constants.PATH_HERO_KNIGHT_IDLE);
                    if (sheet != null) {
                        avatar = sheet.getSubimage(0, 0, 96, 84); // Tách frame đầu
                    }
                }

                if (avatar != null) {
                    int pad = 4;
                    g2d.drawImage(avatar, slotX + pad, slotY + pad, slotSize - pad * 2, slotSize - pad * 2, null);
                }

                // Draw Name and Stats
                g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
                g2d.setColor(Constants.COLOR_DARK_BROWN);
                g2d.drawString(actor.getName(), slotX + slotSize + 15, slotY + 20);

                g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
                g2d.setColor(Color.BLACK); // Thống nhất để màu đen cho đơn giản
                g2d.drawString("HP: " + actor.getCurrentHp() + "/" + actor.getStats().getMaxHp(), slotX + slotSize + 15,
                        slotY + 40);
            }
        };
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(300, 0));
        return left;
    }

    // ─────────────────────────────────────────────────────────
    // RIGHT PANEL (Action Buttons)
    // ─────────────────────────────────────────────────────────
    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 15, 18));
        right.setOpaque(false);
        right.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        for (int i = 0; i < ACTIONS.length; i++) {
            final int idx = i;
            right.add(new ActionButton(ACTIONS[i], slotImage, () -> {
                swingabyss.model.Entity actor = turnManager.getCurrentActor();
                if (actor == null || turnManager.getCurrentState() != swingabyss.manager.GameState.HERO_ACTION) {
                    return; // Chỉ cho phép bấm khi đến lượt Hero
                }

                if (idx == 0) {
                    // Attack -> Đổi sang trạng thái ngắm bắn mục tiêu
                    turnManager.changeState(swingabyss.manager.GameState.SELECT_TARGET);
                    System.out.println("[UIPanel] Entered SELECT_TARGET mode.");
                } else if (idx == 1) {
                    // Defend -> Thực thi luôn
                    turnManager.pushCommand(new swingabyss.controller.DefendCommand(actor));
                } else if (idx == 2) {
                    // Heal -> Thực thi luôn
                    turnManager.pushCommand(new swingabyss.controller.HealCommand(actor));
                }
            }));
        }
        return right;
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
            this.label = label;
            this.slotImg = slotImg;
            this.action = action;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(110, 45)); // Smaller button size

            // Mouse interaction — will become Command dispatchers later
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hovered = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {
                    if (hovered) {
                        pressed = false;
                        action.run();
                    }
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
            int sL = SLOT_INSETS.left, sT = SLOT_INSETS.top;
            int sR = sw - SLOT_INSETS.right, sB = sh - SLOT_INSETS.bottom;
            int dL = sL, dT = sT, dR = dw - SLOT_INSETS.right, dB = dh - SLOT_INSETS.bottom;

            if (dR <= dL || dB <= dT) {
                g2d.drawImage(slotImg, dx, dy, dw, dh, null);
                return;
            }

            // 9 regions (same algorithm as NineSlicePanel)
            drawR(g2d, slotImg, dx, dy, dL, dT, 0, 0, sL, sT);
            drawR(g2d, slotImg, dx + dL, dy, dR - dL, dT, sL, 0, sR - sL, sT);
            drawR(g2d, slotImg, dx + dR, dy, dw - dR, dT, sR, 0, sw - sR, sT);
            drawR(g2d, slotImg, dx, dy + dT, dL, dB - dT, 0, sT, sL, sB - sT);
            drawR(g2d, slotImg, dx + dL, dy + dT, dR - dL, dB - dT, sL, sT, sR - sL, sB - sT);
            drawR(g2d, slotImg, dx + dR, dy + dT, dw - dR, dB - dT, sR, sT, sw - sR, sB - sT);
            drawR(g2d, slotImg, dx, dy + dB, dL, dh - dB, 0, sB, sL, sh - sB);
            drawR(g2d, slotImg, dx + dL, dy + dB, dR - dL, dh - dB, sL, sB, sR - sL, sh - sB);
            drawR(g2d, slotImg, dx + dR, dy + dB, dw - dR, dh - dB, sR, sB, sw - sR, sh - sB);
        }

        private void drawR(Graphics2D g, BufferedImage img,
                int dx, int dy, int dw, int dh,
                int sx, int sy, int sw, int sh) {
            if (dw <= 0 || dh <= 0 || sw <= 0 || sh <= 0)
                return;
            g.drawImage(img, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
        }
    }
}
