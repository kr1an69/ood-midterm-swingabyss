package swingabyss.view;

import swingabyss.utils.Constants;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * MainFrame — the top-level application window.
 *
 * Wires together the two main panels via Composition (not inheritance):
 *   ┌─────────────────────────────┐  ← MainFrame (JFrame)
 *   │  GamePanel (450px tall)     │  ← Arena with sprites & background
 *   │─────────────────────────────│
 *   │  UIPanel  (110px tall)      │  ← Action menu with parchment frame
 *   └─────────────────────────────┘
 *
 * Window is fixed-size (no resize) to maintain pixel-perfect scaling.
 * Centering uses setLocationRelativeTo(null) after pack().
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private GamePanel gamePanel;
    private UIPanel   uiPanel;

    public MainFrame() {
        setTitle("⚔  Swing into the Abyss — v0.2 Asset Preview");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // ── Instantiate panels ───────────────────────────────
        gamePanel = new GamePanel();
        uiPanel   = new UIPanel();

        // Fix UIPanel height (GamePanel uses its own preferred size)
        uiPanel.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.UI_HEIGHT));

        // ── Assemble content pane ────────────────────────────
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBackground(java.awt.Color.BLACK);

        // Thin margin around UIPanel so it "floats" off the window edge
        JPanel uiWrapper = new JPanel(new BorderLayout());
        uiWrapper.setOpaque(false);
        uiWrapper.setBorder(new EmptyBorder(4, 8, 8, 8));
        uiWrapper.add(uiPanel, BorderLayout.CENTER);

        contentPane.add(gamePanel, BorderLayout.CENTER);
        contentPane.add(uiWrapper, BorderLayout.SOUTH);

        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null); // Center on screen
    }
}
