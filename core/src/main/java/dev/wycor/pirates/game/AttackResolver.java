package dev.wycor.pirates.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Resolves a full combat round: player attack, then optional monster counterattack. */
public final class AttackResolver {
    private final Random combatRandom;

    public AttackResolver(Random combatRandom) {
        this.combatRandom = combatRandom;
    }

    List<Attack> resolveCombatRound(Player player, Monster opponent, Weapon weapon) {
        ArrayList<Attack> attacksThisRound = new ArrayList<>(2);

        Attack playerAttack = new Attack(player, opponent, weapon);
        attacksThisRound.add(opponent.receiveAttack(playerAttack, this.combatRandom));

        if (!opponent.isDead()) {
            attacksThisRound.add(opponent.strike(player, this.combatRandom));
        }

        return attacksThisRound;
    }
}
