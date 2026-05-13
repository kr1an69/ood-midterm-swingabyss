package swingabyss.manager;

import swingabyss.model.Entity;

/**
 * Interface to listen to combat events from the TurnManager.
 * Allows the View to trigger animations when the Model executes actions.
 */
public interface ICombatListener {
    void onAttack(Entity attacker, Entity target);
    void onHeal(Entity target);
    void onDefend(Entity target);
}
