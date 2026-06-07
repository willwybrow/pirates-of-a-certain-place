package dev.wycor.pirates.game;

public class CombatantDetails {
    private final String name;
    private final int health;
    private final int maxHealth;

    public CombatantDetails(String name, int health, int maxHealth) {
        this.name = name;
        this.health = health;
        this.maxHealth = maxHealth;
    }

    public String name() {
        return name;
    }

    public int health() {
        return health;
    }

    public int maxHealth() {
        return maxHealth;
    }
}
