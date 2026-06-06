package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

class Player extends Combatant {

    private static final int INITIAL_HEALTH = 20;
    private static final int INITIAL_FOOD = 20;
    private static final int STARVATION_DAMAGE_PER_MOVE = 5;

    private Hex position;
    private int food;

    Player(Hex initialPosition) {
        super("You", INITIAL_HEALTH, 5, 0);
        this.position = initialPosition;
        this.food = INITIAL_FOOD;
    }

    Hex position() {
        return this.position;
    }

    void moveTo(Hex position) {
        this.position = position;
    }

    @Override
    Attack receiveAttackFrom(Combatant combatant) {
        Attack attackReceived = new Attack(combatant, this, combatant.attack, this.defence);
        this.health = Math.max(0, this.health - attackReceived.actualDamage());
        return attackReceived;
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

    void consumeTravelSupplies() {
        if (this.food > 0) {
            this.food -= 1;
            return;
        }

        this.health = Math.max(0, this.health - STARVATION_DAMAGE_PER_MOVE);
    }
}
