package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

public class GiantSquid extends Monster {
    public GiantSquid() {
        super("Giant Squid", 61);
    }

    @Override
    protected Weapon strikeWeapon() {
        return Weapon.SQUID_STRIKE;
    }

    @Override
    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.HARPOON) {
            return WeaponEffectiveness.SUPER_EFFECTIVE;
        }

        return super.effectivenessAgainst(weapon);
    }
}
