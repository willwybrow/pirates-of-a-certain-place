package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

import java.util.Random;

public class SeaweedMonster extends Monster {
    public static final int MIN_HEALTH = 97;
    public static final int MAX_HEALTH = 103;

    private SeaweedMonster(int health) {
        super("Seaweed Monster", health);
    }

    public static SeaweedMonster withHealth(int health) {
        return new SeaweedMonster(health);
    }

    public static int rollHealth(Random random) {
        return randomInclusive(random, MIN_HEALTH, MAX_HEALTH);
    }

    @Override
    protected Weapon strikeWeapon() {
        return Weapon.SEAWEED_STRIKE;
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
