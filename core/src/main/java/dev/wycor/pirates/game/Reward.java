package dev.wycor.pirates.game;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Reward {
    private final int health;
    private final int food;
    private final EnumMap<Weapon, Integer> ammunitionByWeapon;
    private final Treasure treasure;

    public Reward(int health, int food) {
        this(health, food, new EnumMap<>(Weapon.class), null);
    }

    public Reward(int health, int food, int ammunitionPerWeapon) {
        this(health, food, createAmmunitionByWeapon(ammunitionPerWeapon), null);
    }

    public Reward(int health, int food, Map<Weapon, Integer> ammunitionByWeapon) {
        this(health, food, ammunitionByWeapon, null);
    }

    public Reward(Treasure treasure) {
        this(0, 0, new EnumMap<>(Weapon.class), treasure);
    }

    public Reward(int health, int food, Map<Weapon, Integer> ammunitionByWeapon, Treasure treasure) {
        this.health = health;
        this.food = food;
        this.ammunitionByWeapon = new EnumMap<>(ammunitionByWeapon);
        this.treasure = treasure;
    }

    public int health() {
        return health;
    }

    public int food() {
        return food;
    }

    public Map<Weapon, Integer> ammunitionByWeapon() {
        return Collections.unmodifiableMap(this.ammunitionByWeapon);
    }

    public Treasure treasure() {
        return this.treasure;
    }

    public boolean hasAny() {
        return health != 0 || food != 0 || !ammunitionByWeapon.isEmpty() || treasure != null;
    }

    private static EnumMap<Weapon, Integer> createAmmunitionByWeapon(int ammunitionPerWeapon) {
        EnumMap<Weapon, Integer> ammunitionByWeapon = new EnumMap<>(Weapon.class);
        if (ammunitionPerWeapon > 0) {
            for (Weapon weapon : Weapon.values()) {
                if (weapon.usesAmmunition()) {
                    ammunitionByWeapon.put(weapon, ammunitionPerWeapon);
                }
            }
        }
        return ammunitionByWeapon;
    }
}
