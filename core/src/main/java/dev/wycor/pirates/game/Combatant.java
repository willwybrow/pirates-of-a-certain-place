package dev.wycor.pirates.game;

import java.util.List;

public abstract class Combatant {
    final String name;
    int health;
    final int maxHealth;

    protected Combatant(String name, int health) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
    }

    abstract Attack receiveAttack(Attack attack, java.util.Random combatRandom);

    /** The weapons this combatant is able to attack with. */
    abstract List<Weapon> wieldableWeapons();

    boolean canWield(Weapon weapon) {
        return wieldableWeapons().contains(weapon);
    }

    String name() {
        return this.name;
    }

    int health() {
        return this.health;
    }

    int maxHealth() {
        return this.maxHealth;
    }

    public boolean isDead() {
        return this.health <= 0;
    }
}
