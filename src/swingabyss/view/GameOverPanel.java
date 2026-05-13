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
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class GameOverPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private TurnManager turnManager;

    public GameOverPanel(TurnManager turnManager, Runnable onReturnMenu) {
        this.turnManager = turnManager;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JPanel buttonContainer = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        buttonContainer.setOpaque(false);
        buttonContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 150, 0)); 

        BufferedImage slotImg = SpriteLoader.getInstance().loadImage(Constants.UI_SLOT);
        buttonContainer.add(new GameOverButton("Return to Menu", slotImg, onReturnMenu));

        add(buttonContainer, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Chữ GAME OVER
        g2d.setFont(new Font("Monospaced", Font.BOLD, 80));
        String text = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2 - 50;

        g2d.setColor(new Color(150, 0, 0)); // Đổ bóng viền
        g2d.drawString(text, x + 5, y + 5);
        g2d.setColor(Color.RED);
        g2d.drawString(text, x, y);

        // Hiển thị Kỷ lục Wave
        g2d.setFont(new Font("Monospaced", Font.BOLD, 30));
        String subText = "Wave Survived: " + turnManager.getCurrentWave();
        FontMetrics fmSub = g2d.getFontMetrics();
        int subX = (getWidth() - fmSub.stringWidth(subText)) / 2;
        int subY = y + 80;

        g2d.setColor(Color.GRAY);
        g2d.drawString(subText, subX + 2, subY + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(subText, subX, subY);
    }

    private static class GameOverButton extends JPanel {
        private static final long serialVersionUID = 1L;
        private final String label;
        private final BufferedImage slotImg;
        private final Runnable action;
        private boolean hovered = false;
        private boolean pressed = false;
        private static final java.awt.Insets SLOT_INSETS = new java.awt.Insets(4, 4, 4, 4);

        GameOverButton(String label, BufferedImage slotImg, Runnable action) {
            this.label = label;
            this.slotImg = slotImg;
            this.action = action;
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(250, 60)); 

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true; repaint(); }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) { hovered = false; pressed = false; repaint(); }
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) { pressed = true; repaint(); }
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
            int ox = pressed ? 1 : 0;
            int oy = pressed ? 2 : 0;
            drawSlot(g2d, ox, oy, getWidth() - ox, getHeight() - oy);

            if (hovered && !pressed) {
                g2d.setColor(new Color(255, 240, 180, 50));
                g2d.fillRoundRect(ox + 2, oy + 2, getWidth() - ox - 4, getHeight() - oy - 4, 4, 4);
            }

            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(label)) / 2 + ox;
            int textY = (getHeight() + fm.getAscent() - fm.getDescent()) / 2 + oy;

            g2d.setColor(Constants.COLOR_DARK_BROWN.darker());
            g2d.drawString(label, textX + 2, textY + 2);
            g2d.setColor(new Color(0xF5E6C8));
            g2d.drawString(label, textX, textY);
        }

        private void drawSlot(Graphics2D g2d, int dx, int dy, int dw, int dh) {
            if (slotImg == null) return;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int sw = slotImg.getWidth(), sh = slotImg.getHeight();
            int sL = SLOT_INSETS.left, sT = SLOT_INSETS.top;
            int sR = sw - SLOT_INSETS.right, sB = sh - SLOT_INSETS.bottom;
            int dL = sL, dT = sT, dR = dw - SLOT_INSETS.right, dB = dh - SLOT_INSETS.bottom;

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

        private void drawR(Graphics2D g, BufferedImage img, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh) {
            if (dw <= 0 || dh <= 0 || sw <= 0 || sh <= 0) return;
            g.drawImage(img, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
        }
    }
}
