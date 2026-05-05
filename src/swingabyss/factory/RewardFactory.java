package swingabyss.factory;

import swingabyss.model.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RewardFactory {

    public interface RewardEffect {
        void apply(List<Hero> heroes);
    }

    public static class Reward {
        private String title;
        private String description;
        private RewardEffect effect;

        public Reward(String title, String description, RewardEffect effect) {
            this.title = title;
            this.description = description;
            this.effect = effect;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public void applyEffect(List<Hero> heroes) {
            if (effect != null) {
                effect.apply(heroes);
            }
        }
    }

    public static List<Reward> generateRewards() {
        List<Reward> allPossibleRewards = new ArrayList<>();

        allPossibleRewards.add(new Reward("VIT UP", "+20 Max HP for all Heroes", heroes -> {
            for (Hero h : heroes) {
                h.getStats().setMaxHp(h.getStats().getMaxHp() + 20);
                h.heal(20);
            }
        }));

        allPossibleRewards.add(new Reward("ATK UP", "+5 Attack for all Heroes", heroes -> {
            for (Hero h : heroes) {
                h.getStats().setAttack(h.getStats().getAttack() + 5);
            }
        }));

        allPossibleRewards.add(new Reward("DEF UP", "+3 Defense for all Heroes", heroes -> {
            for (Hero h : heroes) {
                h.getStats().setDefense(h.getStats().getDefense() + 3);
            }
        }));

        allPossibleRewards.add(new Reward("HEAL ALL", "Restore 50% HP for all Heroes", heroes -> {
            for (Hero h : heroes) {
                int healAmount = h.getStats().getMaxHp() / 2;
                h.heal(healAmount);
            }
        }));

        allPossibleRewards.add(new Reward("SPD UP", "+10 Speed for all Heroes", heroes -> {
            for (Hero h : heroes) {
                h.getStats().setSpeed(h.getStats().getSpeed() + 10);
            }
        }));

        allPossibleRewards.add(new Reward("CHARGE UP", "+3 Heal Charge for all Heroes", heroes -> {
            for (Hero h : heroes) {
                h.addMaxHealCharges(3);
            }
        }));

        // Shuffle and pick 3
        Collections.shuffle(allPossibleRewards);
        return allPossibleRewards.subList(0, 3);
    }
}
