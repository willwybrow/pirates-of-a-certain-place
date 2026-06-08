package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

public class GhostShip extends Monster {
    public GhostShip() {
        super("Ghost Ship", 100);
    }

    @Override
    protected Weapon strikeWeapon() {
        return Weapon.GHOST_STRIKE;
    }

    @Override
    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.FLAMING_ARROWS) {
            return WeaponEffectiveness.SUPER_EFFECTIVE;
        }

        return WeaponEffectiveness.NOT_VERY_EFFECTIVE;
    }
}
