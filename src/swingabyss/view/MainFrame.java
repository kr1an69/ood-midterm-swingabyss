package swingabyss.view;

import swingabyss.utils.Constants;
import swingabyss.manager.TurnManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * MainFrame — the top-level application window.
 *
 * Wires together the panels using CardLayout:
 * - MENU: MainMenuPanel
 * - GAME: GamePanel + UIPanel
 * - GAMEOVER: GameOverPanel
 */
public class MainFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private CardLayout cardLayout;
    private JPanel contentPane;
    
    private MainMenuPanel mainMenuPanel;
    private GamePanel gamePanel;
    private UIPanel uiPanel;
    private GameOverPanel gameOverPanel;
    
    private TurnManager turnManager;

    public MainFrame(TurnManager turnManager) {
        this.turnManager = turnManager;
        setTitle("Swing into the Abyss");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        cardLayout = new CardLayout();
        contentPane = new JPanel(cardLayout);
        contentPane.setBackground(java.awt.Color.BLACK);

        // ── 1. Khởi tạo Game Wrapper (GamePanel + UIPanel) ───────
        gamePanel = new GamePanel(turnManager);
        uiPanel = new UIPanel(turnManager);
        uiPanel.setPreferredSize(new Dimension(Constants.WINDOW_WIDTH, Constants.UI_HEIGHT));

        JPanel gameWrapper = new JPanel(new BorderLayout());
        gameWrapper.setBackground(java.awt.Color.BLACK);
        JPanel uiWrapper = new JPanel(new BorderLayout());
        uiWrapper.setOpaque(false);
        uiWrapper.setBorder(new EmptyBorder(4, 8, 8, 8));
        uiWrapper.add(uiPanel, BorderLayout.CENTER);
        
        gameWrapper.add(gamePanel, BorderLayout.CENTER);
        gameWrapper.add(uiWrapper, BorderLayout.SOUTH);

        // ── 2. Khởi tạo Main Menu ────────────────────────────────
        mainMenuPanel = new MainMenuPanel(
            () -> { // onStart
                switchCard("GAME");
                turnManager.startGame();
            },
            () -> { // onExit
                System.exit(0);
            }
        );

        // ── 3. Khởi tạo Game Over ────────────────────────────────
        gameOverPanel = new GameOverPanel(
            turnManager,
            () -> { // onReturnMenu
                turnManager.resetGame();
                switchCard("MENU");
            }
        );

        // ── Thêm vào CardLayout ──────────────────────────────────
        contentPane.add(mainMenuPanel, "MENU");
        contentPane.add(gameWrapper, "GAME");
        contentPane.add(gameOverPanel, "GAMEOVER");

        setContentPane(contentPane);
        pack();
        setLocationRelativeTo(null); // Center on screen

        // ── Đăng ký sự kiện Game Over ────────────────────────────
        turnManager.setOnGameOverCallback(() -> {
            switchCard("GAMEOVER");
        });
    }

    public void switchCard(String cardName) {
        cardLayout.show(contentPane, cardName);
    }
}
