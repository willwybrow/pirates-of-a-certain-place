package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class PlayerDetails {
    private final Hex position;
    private final int health;
    private final int maxHealth;
    private final int food;
    private final EnumMap<Treasure, Boolean> capturedTreasures;
    private final EnumMap<Weapon, Integer> ammunitionByWeapon;
    private final List<Weapon> wieldableWeapons;

    public PlayerDetails(Hex position, int health, int maxHealth, int food, Map<Treasure, Boolean> capturedTreasures,
                         Map<Weapon, Integer> ammunitionByWeapon, List<Weapon> wieldableWeapons) {
        this.position = position;
        this.health = health;
        this.maxHealth = maxHealth;
        this.food = food;
        this.capturedTreasures = new EnumMap<>(capturedTreasures);
        this.ammunitionByWeapon = new EnumMap<>(ammunitionByWeapon);
        this.wieldableWeapons = Collections.unmodifiableList(new ArrayList<>(wieldableWeapons));
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

    public Map<Weapon, Integer> ammunitionByWeapon() {
        return Collections.unmodifiableMap(this.ammunitionByWeapon);
    }

    public List<Weapon> wieldableWeapons() {
        return this.wieldableWeapons;
    }

    public boolean isGameOver() {
        if (health <= 0 || food <= 0) {
            return true;
        }

        for (Boolean captured : capturedTreasures.values()) {
            if (!captured) {
                return false;
            }
        }

        return true;
    }
}
