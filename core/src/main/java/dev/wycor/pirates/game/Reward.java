package dev.wycor.pirates.game;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class Reward {
    private static final Reward NONE = new Reward(0, 0, new EnumMap<>(Weapon.class));

    private final int health;
    private final int food;
    private final EnumMap<Weapon, Integer> ammunitionByWeapon;

    public Reward(int health, int food) {
        this(health, food, new EnumMap<>(Weapon.class));
    }

    private Reward(int health, int food, EnumMap<Weapon, Integer> ammunitionByWeapon) {
        this.health = health;
        this.food = food;
        this.ammunitionByWeapon = new EnumMap<>(ammunitionByWeapon);
    }

    public static Reward none() {
        return NONE;
    }

    public static Reward food(int amount) {
        return new Reward(0, amount);
    }

    public static Reward health(int amount) {
        return new Reward(amount, 0);
    }

    public static Reward islandSupplies(int foodAmount, int ammunitionPerWeapon) {
        EnumMap<Weapon, Integer> ammunitionByWeapon = new EnumMap<>(Weapon.class);
        if (ammunitionPerWeapon > 0) {
            for (Weapon weapon : Weapon.values()) {
                if (weapon.usesAmmunition()) {
                    ammunitionByWeapon.put(weapon, ammunitionPerWeapon);
                }
            }
        }
        return new Reward(0, foodAmount, ammunitionByWeapon);
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

    public boolean hasAny() {
        return health != 0 || food != 0 || !ammunitionByWeapon.isEmpty();
    }
}
