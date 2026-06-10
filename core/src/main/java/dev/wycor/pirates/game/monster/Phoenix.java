package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

import java.util.Random;

public class Phoenix extends Monster {
    public static final int MIN_HEALTH = 66;
    public static final int MAX_HEALTH = 68;

    private Phoenix(int health) {
        super("Phoenix", health);
    }

    public static Phoenix withHealth(int health) {
        return new Phoenix(health);
    }

    public static int rollHealth(Random random) {
        return randomInclusive(random, MIN_HEALTH, MAX_HEALTH);
    }

    @Override
    protected Weapon strikeWeapon() {
        return Weapon.PHOENIX_STRIKE;
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
