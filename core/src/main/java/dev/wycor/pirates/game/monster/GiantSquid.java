package dev.wycor.pirates.game.monster;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Weapon;

import java.util.Random;

public class GiantSquid extends Monster {
    public static final int MIN_HEALTH = 61;
    public static final int MAX_HEALTH = 64;

    public GiantSquid() {
        this(MIN_HEALTH);
    }

    private GiantSquid(int health) {
        super("Giant Squid", health);
    }

    public static GiantSquid withHealth(int health) {
        return new GiantSquid(health);
    }

    public static int rollHealth(Random random) {
        return randomInclusive(random, MIN_HEALTH, MAX_HEALTH);
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
