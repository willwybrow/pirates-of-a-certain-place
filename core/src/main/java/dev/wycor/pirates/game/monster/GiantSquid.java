package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

public class GiantSquid extends Monster {
    public GiantSquid() {
        super("Giant Squid", 130, 10, 0);
    }

    @Override
    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.HARPOON) {
            return WeaponEffectiveness.SUPER_EFFECTIVE;
        }

        return super.effectivenessAgainst(weapon);
    }
}
