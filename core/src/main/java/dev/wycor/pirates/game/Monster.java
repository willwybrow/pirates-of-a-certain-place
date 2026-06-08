package dev.wycor.pirates.game;

public abstract class Monster extends Combatant {

    protected enum WeaponEffectiveness {
        SUPER_EFFECTIVE(3.0),
        MEDIUM_EFFECTIVE(2.0),
        NOT_VERY_EFFECTIVE(0.5);

        private final double damageMultiplier;

        WeaponEffectiveness(double damageMultiplier) {
            this.damageMultiplier = damageMultiplier;
        }

        double damageMultiplier() {
            return this.damageMultiplier;
        }
    }

    protected Monster(String name, int health, int attack, int defence) {
        super(name, health, attack, defence);
    }

    @Override
    Attack receiveAttack(Attack attack) {
        int mitigatedBaseDamage = Math.max(0, attack.unmitigatedAttackDamage() - attack.defenderMitigation());
        int effectivenessAdjustedDamage = mitigatedBaseDamage;
        if (attack.weapon() != null) {
            WeaponEffectiveness effectiveness = this.effectivenessAgainst(attack.weapon());
            effectivenessAdjustedDamage = CombatDamage.scaleByMultiplier(mitigatedBaseDamage, effectiveness.damageMultiplier());
        }

        int randomizedDamage = CombatDamage.withVariance(effectivenessAdjustedDamage);
        attack.setActualDamage(randomizedDamage);
        this.health = Math.max(0, this.health - randomizedDamage);
        return attack;
    }

    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.CUTLASS) {
            return WeaponEffectiveness.NOT_VERY_EFFECTIVE;
        }

        return WeaponEffectiveness.MEDIUM_EFFECTIVE;
    }
}
