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
            heroes.add(new swingabyss.model.Hero("Swordswoman", new swingabyss.model.Stats(80, 25, 5, 120)));
            heroes.add(new swingabyss.model.Hero("Magician", new swingabyss.model.Stats(60, 30, 2, 90)));

            // Khởi tạo FSM Manager (Monsters sẽ được sinh bởi MonsterFactory)
            List<swingabyss.model.Monster> emptyMonsters = new ArrayList<>();
            swingabyss.manager.TurnManager turnManager = new swingabyss.manager.TurnManager(heroes, emptyMonsters);
            turnManager.startGame(); // Start the first wave

            // Inject dependency vào View
            MainFrame frame = new MainFrame(turnManager);
            frame.setVisible(true);
        });
    }
}
