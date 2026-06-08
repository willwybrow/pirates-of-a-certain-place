package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

public class SeaweedMonster extends Monster {
    public SeaweedMonster() {
        super("Seaweed Monster", 100, 10, 0);
    }

    @Override
    protected WeaponEffectiveness effectivenessAgainst(Weapon weapon) {
        if (weapon == Weapon.GIANT_AXE) {
            return WeaponEffectiveness.SUPER_EFFECTIVE;
        }

        if (weapon == Weapon.HARPOON) {
            return WeaponEffectiveness.NOT_VERY_EFFECTIVE;
        }

        return super.effectivenessAgainst(weapon);
    }
}
