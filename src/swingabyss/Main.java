package swingabyss;

import javax.swing.SwingUtilities;
import swingabyss.view.MainFrame;

public class Main {
    public static void main(String[] args) {
        // Build the GUI on the Event Dispatch Thread to ensure Thread Safety
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
