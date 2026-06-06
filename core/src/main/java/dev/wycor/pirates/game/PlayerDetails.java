package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

public class PlayerDetails {
    private final Hex position;
    private final int health;
    private final int maxHealth;
    private final int food;

    public PlayerDetails(Hex position, int health, int maxHealth, int food) {
        this.position = position;
        this.health = health;
        this.maxHealth = maxHealth;
        this.food = food;
    }

    public Hex position() {
        return position;
    }

    public int health() {
        return health;
    }

    public int maxHealth() {
        return maxHealth;
    }

    public int food() {
        return food;
    }

    public boolean isGameOver() {
        return health <= 0;
    }
}
