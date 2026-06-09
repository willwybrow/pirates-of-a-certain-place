package dev.wycor.pirates.game;

import java.util.Random;

final class CombatDamage {
    private CombatDamage() {
    }

    static int withVariance(int baseDamage, Weapon weapon, Random random) {
        int boundedBaseDamage = Math.max(0, baseDamage);
        int varianceRange = (weapon.maxVariance() - weapon.minVariance()) + 1;
        int randomVariance = random.nextInt(varianceRange) + weapon.minVariance();
        return Math.max(0, boundedBaseDamage + randomVariance);
    }

    static int scaleByMultiplier(int baseDamage, double multiplier) {
        return Math.max(0, (int) Math.round(baseDamage * multiplier));
    }
}
