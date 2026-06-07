package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

class Player extends Combatant {

    private static final int INITIAL_HEALTH = 100;
    private static final int INITIAL_FOOD = 20;

    private Hex position;
    private int food;
    private final EnumMap<Weapon, Integer> ammunitionByWeapon;
    private final EnumMap<Treasure, Boolean> capturedTreasures;

    Player(Hex initialPosition) {
        super("You", INITIAL_HEALTH, 5, 0);
        this.position = initialPosition;
        this.food = INITIAL_FOOD;
        this.ammunitionByWeapon = new EnumMap<>(Weapon.class);
        this.capturedTreasures = new EnumMap<>(Treasure.class);

        for (Weapon weapon : Weapon.values()) {
            if (weapon.usesAmmunition()) {
                this.ammunitionByWeapon.put(weapon, 0);
            }
        }

        for (Treasure treasure : Treasure.values()) {
            this.capturedTreasures.put(treasure, false);
        }
    }

    Hex position() {
        return this.position;
    }

    void moveTo(Hex position) {
        this.position = position;
    }

    @Override
    Attack receiveAttack(Attack attack) {
        this.health = Math.max(0, this.health - attack.actualDamage());
        return attack;
    }

    void heal(int amount) {
        this.health = Math.min(INITIAL_HEALTH, this.health + amount);
    }

    int maxHealth() {
        return INITIAL_HEALTH;
    }

    int food() {
        return this.food;
    }

    void restock(int amount) {
        if (amount > 0) {
            this.food += amount;
        }
    }

    void restockAmmunition(Weapon weapon, int amount) {
        if (amount <= 0 || !weapon.usesAmmunition()) {
            return;
        }

        this.ammunitionByWeapon.merge(weapon, amount, Integer::sum);
    }

    boolean consumeAmmunition(Weapon weapon) {
        if (!weapon.usesAmmunition()) {
            return true;
        }

        int currentAmmunition = this.ammunitionByWeapon.getOrDefault(weapon, 0);
        if (currentAmmunition <= 0) {
            return false;
        }

        this.ammunitionByWeapon.put(weapon, currentAmmunition - 1);
        return true;
    }

    void captureTreasure(Treasure treasure) {
        this.capturedTreasures.put(treasure, true);
    }

    Map<Treasure, Boolean> capturedTreasures() {
        return Collections.unmodifiableMap(this.capturedTreasures);
    }

    Map<Weapon, Integer> ammunitionByWeapon() {
        return Collections.unmodifiableMap(this.ammunitionByWeapon);
    }

    boolean hasCapturedAllTreasures() {
        for (Boolean captured : this.capturedTreasures.values()) {
            if (!captured) {
                return false;
            }
        }
        return true;
    }

    void consumeTravelSupplies() {
        if (this.food > 0) {
            this.food -= 1;
        }
    }
}
