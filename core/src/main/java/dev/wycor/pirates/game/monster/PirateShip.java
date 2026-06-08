package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

public class PirateShip extends Monster {
    public PirateShip() {
        super("Pirate Ship", 110);
    }

    @Override
    protected Weapon strikeWeapon() {
        return Weapon.PIRATE_STRIKE;
    }

    @Override
    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.CANNON) {
            return WeaponEffectiveness.SUPER_EFFECTIVE;
        }

        return super.effectivenessAgainst(weapon);
    }
}
