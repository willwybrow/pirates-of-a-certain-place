package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

import java.util.Random;

public class PirateShip extends Monster {
    public static final int MIN_HEALTH = 57;
    public static final int MAX_HEALTH = 62;

    private PirateShip(int health) {
        super("Pirate Ship", health);
    }

    public static PirateShip withHealth(int health) {
        return new PirateShip(health);
    }

    public static int rollHealth(Random random) {
        return randomInclusive(random, MIN_HEALTH, MAX_HEALTH);
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
