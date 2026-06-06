package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class PlayerDetails {
    private final Hex position;
    private final int health;
    private final int maxHealth;
    private final int food;
    private final EnumMap<Treasure, Boolean> capturedTreasures;

    public PlayerDetails(Hex position, int health, int maxHealth, int food, Map<Treasure, Boolean> capturedTreasures) {
        this.position = position;
        this.health = health;
        this.maxHealth = maxHealth;
        this.food = food;
        this.capturedTreasures = new EnumMap<>(capturedTreasures);
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

    public Map<Treasure, Boolean> capturedTreasures() {
        return Collections.unmodifiableMap(this.capturedTreasures);
    }

    public boolean isGameOver() {
        return health <= 0;
    }
}
