package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.EnumMap;

class Player extends Combatant {

    private static final int INITIAL_HEALTH = 20;
    private static final int INITIAL_FOOD = 20;
    private static final int STARVATION_DAMAGE_PER_MOVE = 5;

    private Hex position;
    private int food;
    private final EnumMap<Weapon, Integer> ammunitionByWeapon;

    Player(Hex initialPosition) {
        super("You", INITIAL_HEALTH, 5, 0);
        this.position = initialPosition;
        this.food = INITIAL_FOOD;
        this.ammunitionByWeapon = new EnumMap<>(Weapon.class);

        for (Weapon weapon : Weapon.values()) {
            if (weapon.usesAmmunition()) {
                this.ammunitionByWeapon.put(weapon, 0);
            }
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

    void consumeTravelSupplies() {
        if (this.food > 0) {
            this.food -= 1;
            return;
        }

        this.health = Math.max(0, this.health - STARVATION_DAMAGE_PER_MOVE);
    }
}
