package dev.wycor.pirates.game;

import java.util.ArrayList;

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
}
