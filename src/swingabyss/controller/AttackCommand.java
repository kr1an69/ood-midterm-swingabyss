package swingabyss.controller;

import swingabyss.model.Entity;

/**
 * Lớp đại diện cho hành động "Tấn công" cơ bản.
 * Chứa mục tiêu tấn công và người tiến hành tấn công.
 */
public class AttackCommand implements ICommand {
    private Entity attacker;
    private Entity target;

    public AttackCommand(Entity attacker, Entity target) {
        this.attacker = attacker;
        this.target = target;
    }

    @Override
    public void execute() {
        if (!attacker.isDead() && !target.isDead()) {
            System.out.println("[CMD] " + attacker.getName() + " tan cong " + target.getName() + "!");
            int damage = attacker.getStats().getAttack();
            target.takeDamage(damage);
        }
    }
}
