package swingabyss.factory;

import swingabyss.model.Monster;
import swingabyss.model.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonsterFactory {
    private static final String[] MONSTER_TYPES = {"beast", "demon", "dragon", "ghost", "goblin", "ogre"};
    private static final Random RANDOM = new Random();

    public static List<Monster> generateWave(int waveTier) {
        List<Monster> monsters = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            String type = MONSTER_TYPES[RANDOM.nextInt(MONSTER_TYPES.length)];
            
            // Base stats for all monsters
            int baseHp = 100 + RANDOM.nextInt(20);
            int baseAttack = 10 + RANDOM.nextInt(5);
            int baseDefense = 2 + RANDOM.nextInt(3);
            int baseSpeed = 80 + RANDOM.nextInt(20);
            
            Stats stats = new Stats(baseHp, baseAttack, baseDefense, baseSpeed);
            
            // Scale stats based on waveTier (e.g., +20% per wave)
            float multiplier = 1.0f + (waveTier - 1) * 0.2f;
            stats.scaleStats(multiplier);
            
            Monster m = new Monster(type, stats, waveTier);
            monsters.add(m);
        }
        
        return monsters;
    }
}
