package swingabyss;

import javax.swing.SwingUtilities;
import swingabyss.view.MainFrame;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Build the GUI on the Event Dispatch Thread to ensure Thread Safety
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo Model (Entities)
            List<swingabyss.model.Hero> heroes = new ArrayList<>();
            heroes.add(new swingabyss.model.Hero("Knight", new swingabyss.model.Stats(100, 20, 10, 105)));
            heroes.add(new swingabyss.model.Hero("Saber", new swingabyss.model.Stats(80, 25, 5, 120)));
            heroes.add(new swingabyss.model.Hero("Mage", new swingabyss.model.Stats(60, 30, 2, 90)));

            List<swingabyss.model.Monster> monsters = new ArrayList<>();
            monsters.add(new swingabyss.model.Monster("Demon A", new swingabyss.model.Stats(120, 15, 5, 95), 1));
            monsters.add(new swingabyss.model.Monster("Demon B", new swingabyss.model.Stats(120, 15, 5, 90), 1));
            monsters.add(new swingabyss.model.Monster("Demon C", new swingabyss.model.Stats(120, 15, 5, 85), 1));

            // Khởi tạo FSM Manager
            swingabyss.manager.TurnManager turnManager = new swingabyss.manager.TurnManager(heroes, monsters);
            turnManager.startGame(); // Start the first wave

            // Inject dependency vào View
            MainFrame frame = new MainFrame(turnManager);
            frame.setVisible(true);
        });
    }
}
