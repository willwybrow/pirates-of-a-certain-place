package dev.wycor.pirates.game;

import java.util.ArrayList;
import java.util.Collections;
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
            return Collections.emptyList();
        }

        ArrayList<Attack> attacksThisRound = new ArrayList<>(2);

        Attack firstAttack = new Attack(this.firstCombatant, this.secondCombatant, Weapon.CUTLASS, this.firstCombatant.attack, this.secondCombatant.defence);
        this.secondCombatant.receiveAttack(firstAttack);
        combatLog.add(firstAttack);
        attacksThisRound.add(firstAttack);

        if (!this.secondCombatant.isDead()) {
            Attack secondAttack = new Attack(this.secondCombatant, this.firstCombatant, null, this.secondCombatant.attack, this.firstCombatant.defence);
            this.firstCombatant.receiveAttack(secondAttack);
            combatLog.add(secondAttack);
            attacksThisRound.add(secondAttack);
        }

        return Collections.unmodifiableList(new ArrayList<>(attacksThisRound));
    }

    public List<Attack> combatLog() {
        return Collections.unmodifiableList(new ArrayList<>(combatLog));
    }
}
