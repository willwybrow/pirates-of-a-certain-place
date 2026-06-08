package dev.wycor.pirates.game;

/**
 * Test helpers for building bespoke monsters with controllable stats.
 *
 * <p>Lives in the production package so tests can use the package-private {@link Monster} seam.
 */
final class TestMonsters {

    private TestMonsters() {
    }

    /** A plain monster with the given health that strikes with a standard 10-damage strike. */
    static Monster withHealth(String name, int health) {
        return new Monster(name, health) {
            @Override
            protected Weapon strikeWeapon() {
                return Weapon.SQUID_STRIKE;
            }
        };
    }

    /** A harmless monster with the given health whose strikes never damage the target. */
    static Monster harmless(String name, int health) {
        return new Monster(name, health) {
            @Override
            protected Weapon strikeWeapon() {
                return Weapon.SQUID_STRIKE;
            }

            @Override
            Attack strike(Combatant target, java.util.Random combatRandom) {
                Attack strike = new Attack(this, target, strikeWeapon());
                strike.setActualDamage(0);
                return strike;
            }
        };
    }
}
