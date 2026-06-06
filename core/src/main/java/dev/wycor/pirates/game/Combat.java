package dev.wycor.pirates.game;

import java.util.ArrayList;
import java.util.List;

public class Combat {
    private final Combatant firstCombatant;
    private final Combatant secondCombatant;
    private final ArrayList<Attack> combatLog;

    public Combat(Combatant firstCombatant, Combatant secondCombatant) {
        this.firstCombatant = firstCombatant;
        this.secondCombatant = secondCombatant;
        combatLog = new ArrayList<>();
    }

    public boolean isOver() {
        return this.firstCombatant.isDead() || this.secondCombatant.isDead();
    }

    public boolean inProgress() {
        return !isOver();
    }

    public List<Attack> resolveRound() {
        if (isOver()) {
            return List.of();
        }

        ArrayList<Attack> attacksThisRound = new ArrayList<>(2);

        Attack firstAttack = this.secondCombatant.receiveAttackFrom(this.firstCombatant);
        combatLog.add(firstAttack);
        attacksThisRound.add(firstAttack);

        if (!this.secondCombatant.isDead()) {
            Attack secondAttack = this.firstCombatant.receiveAttackFrom(this.secondCombatant);
            combatLog.add(secondAttack);
            attacksThisRound.add(secondAttack);
        }

        return List.copyOf(attacksThisRound);
    }

    public List<Attack> combatLog() {
        return List.copyOf(combatLog);
    }
}
