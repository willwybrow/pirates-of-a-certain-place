package dev.wycor.pirates.game;

import java.util.Random;

final class CombatDamage {
    private static final int MIN_DAMAGE_VARIANCE = -3;
    private static final int MAX_DAMAGE_VARIANCE = 3;

    private CombatDamage() {
    }

    static int withVariance(int baseDamage, Random random) {
        int boundedBaseDamage = Math.max(0, baseDamage);
        int varianceRange = (MAX_DAMAGE_VARIANCE - MIN_DAMAGE_VARIANCE) + 1;
        int randomVariance = random.nextInt(varianceRange) + MIN_DAMAGE_VARIANCE;
        return Math.max(0, boundedBaseDamage + randomVariance);
    }

    static int scaleByMultiplier(int baseDamage, double multiplier) {
        return Math.max(0, (int) Math.round(baseDamage * multiplier));
    }
}
