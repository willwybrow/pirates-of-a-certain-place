package dev.wycor.pirates.game;

public abstract class Combatant {
    final String name;
    int health;
    final int attack;
    final int defence;

    protected Combatant(String name, int health, int attack, int defence) {
        this.name = name;
        this.health = health;
        this.attack = attack;
        this.defence = defence;
    }

    abstract Attack receiveAttackFrom(Combatant combatant);

    boolean isDead() {
        return this.health <= 0;
    }
}
