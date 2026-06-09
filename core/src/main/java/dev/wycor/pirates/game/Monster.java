package dev.wycor.pirates.game;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public abstract class Monster extends Combatant {

    protected enum WeaponEffectiveness {
        SUPER_EFFECTIVE(3.0),
        MEDIUM_EFFECTIVE(2.0),
        NORMAL_EFFECTIVE(1.0),
        NOT_VERY_EFFECTIVE(0.5);

        private final double damageMultiplier;

        WeaponEffectiveness(double damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
        }

        double damageMultiplier() {
            return this.damageMultiplier;
        }
    }

    protected Monster(String name, int health) {
        super(name, health);
    }

    /** The strike this monster retaliates with. Each monster knows its own strike. */
    protected abstract Weapon strikeWeapon();

    @Override
    List<Weapon> wieldableWeapons() {
        return Collections.singletonList(strikeWeapon());
    }

    /** Resolves this monster's retaliatory strike against the given target. */
    Attack strike(Combatant target, java.util.Random combatRandom) {
        Attack strike = new Attack(this, target, strikeWeapon());
        return target.receiveAttack(strike, combatRandom);
    }

    @Override
    Attack receiveAttack(Attack attack, java.util.Random combatRandom) {
        int baseDamage = Math.max(0, attack.baseAttackDamage());
        int effectivenessAdjustedDamage = CombatDamage.scaleByMultiplier(baseDamage,
            this.effectivenessAgainst(attack.weapon()).damageMultiplier());

        int randomizedDamage = CombatDamage.withVariance(effectivenessAdjustedDamage, attack.weapon(), combatRandom);
        attack.setActualDamage(randomizedDamage);
        this.health = Math.max(0, this.health - randomizedDamage);
        return attack;
    }

    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.CUTLASS) {
            return WeaponEffectiveness.NORMAL_EFFECTIVE;
        }

        return WeaponEffectiveness.MEDIUM_EFFECTIVE;
    }

    protected static int randomInclusive(Random random, int minValue, int maxValue) {
        int boundedMin = Math.min(minValue, maxValue);
        int boundedMax = Math.max(minValue, maxValue);
        int range = (boundedMax - boundedMin) + 1;
        return random.nextInt(range) + boundedMin;
    }
}
