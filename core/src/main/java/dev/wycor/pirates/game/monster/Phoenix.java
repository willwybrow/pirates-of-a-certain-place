package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

public class Phoenix extends Monster {
    public Phoenix() {
        super("Phoenix", 70, 10, 0);
    }

    @Override
    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.ICE_DAGGERS) {
            return WeaponEffectiveness.SUPER_EFFECTIVE;
        }

        if (weapon == Weapon.FLAMING_ARROWS) {
            return WeaponEffectiveness.NOT_VERY_EFFECTIVE;
        }

        return super.effectivenessAgainst(weapon);
    }
}
