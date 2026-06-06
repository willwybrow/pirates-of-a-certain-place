package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

public class PlayerDetails extends Combatant {

    private final static int INITIAL_HEALTH = 20;

    private Hex position;

    PlayerDetails(Hex initialPosition) {
        super("You", INITIAL_HEALTH, 5, 0);
        this.position = initialPosition;
    }

    Hex position() {
        return this.position;
    }

    public void moveTo(Hex position) {
        this.position = position;
    }

    @Override
    Attack receiveAttackFrom(Combatant combatant) {
        Attack attackReceived = new Attack(combatant, this, combatant.attack, this.defence);
        this.health = Math.max(0, this.health - attackReceived.actualDamage());
        return attackReceived;
    }

    public void heal(int amount) {
        this.health = Math.min(INITIAL_HEALTH, this.health + amount);
    }

    public int maxHealth() {
        return INITIAL_HEALTH;
    }
}
