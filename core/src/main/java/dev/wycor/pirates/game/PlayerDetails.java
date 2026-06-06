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
        return null;
    }

    public void heal(int amount) {
        this.health = Math.min(INITIAL_HEALTH, this.health + amount);
    }
}
