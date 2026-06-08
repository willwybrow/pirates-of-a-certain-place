package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

class Player extends Combatant {

    private static final int INITIAL_HEALTH = 100;
    private static final int INITIAL_FOOD = 20;
    private static final int INITIAL_AMMUNITION_PER_WEAPON = 1;

    private static final List<Weapon> WIELDABLE_WEAPONS = Collections.unmodifiableList(Arrays.asList(
        Weapon.CUTLASS,
        Weapon.CANNON,
        Weapon.GIANT_AXE,
        Weapon.FLAMING_ARROWS,
        Weapon.HARPOON,
        Weapon.ICE_DAGGERS
    ));

    private Hex position;
    private int food;
    private final EnumMap<Weapon, Integer> ammunitionByWeapon;
    private final EnumMap<Treasure, Boolean> capturedTreasures;

    Player(Hex initialPosition) {
        super("You", INITIAL_HEALTH);
        this.position = initialPosition;
        this.food = INITIAL_FOOD;
        this.ammunitionByWeapon = new EnumMap<>(Weapon.class);
        this.capturedTreasures = new EnumMap<>(Treasure.class);

        for (Weapon weapon : WIELDABLE_WEAPONS) {
            if (weapon.usesAmmunition()) {
                this.ammunitionByWeapon.put(weapon, INITIAL_AMMUNITION_PER_WEAPON);
            }
        }

        for (Treasure treasure : Treasure.values()) {
            this.capturedTreasures.put(treasure, false);
        }
    }

    Hex position() {
        return this.position;
    }

    void moveTo(Hex position) {
        this.position = position;
    }

    @Override
    List<Weapon> wieldableWeapons() {
        return WIELDABLE_WEAPONS;
    }

    @Override
    Attack receiveAttack(Attack attack, java.util.Random combatRandom) {
        int baseDamage = Math.max(0, attack.baseAttackDamage());
        int randomizedDamage = CombatDamage.withVariance(baseDamage, combatRandom);
        attack.setActualDamage(randomizedDamage);
        this.health = Math.max(0, this.health - randomizedDamage);
        return attack;
    }

    void heal(int amount) {
        this.health = Math.min(INITIAL_HEALTH, this.health + amount);
    }

    void takeDamage(int amount) {
        if (amount > 0) {
            this.health = Math.max(0, this.health - amount);
        }
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

    void restockAmmunition(Weapon weapon, int amount) {
        if (amount <= 0 || !weapon.usesAmmunition()) {
            return;
        }

        this.ammunitionByWeapon.merge(weapon, amount, Integer::sum);
    }

    boolean consumeAmmunition(Weapon weapon) {
        if (!weapon.usesAmmunition()) {
            return true;
        }

        int currentAmmunition = this.ammunitionByWeapon.getOrDefault(weapon, 0);
        if (currentAmmunition <= 0) {
            return false;
        }

        this.ammunitionByWeapon.put(weapon, currentAmmunition - 1);
        return true;
    }

    void captureTreasure(Treasure treasure) {
        this.capturedTreasures.put(treasure, true);
    }

    void loseTreasure(Treasure treasure) {
        this.capturedTreasures.put(treasure, false);
    }

    List<Treasure> capturedTreasureList() {
        ArrayList<Treasure> captured = new ArrayList<>();
        for (Map.Entry<Treasure, Boolean> entry : this.capturedTreasures.entrySet()) {
            if (entry.getValue()) {
                captured.add(entry.getKey());
            }
        }
        return captured;
    }

    Map<Treasure, Boolean> capturedTreasures() {
        return Collections.unmodifiableMap(this.capturedTreasures);
    }

    Map<Weapon, Integer> ammunitionByWeapon() {
        return Collections.unmodifiableMap(this.ammunitionByWeapon);
    }

    boolean hasCapturedAllTreasures() {
        for (Boolean captured : this.capturedTreasures.values()) {
            if (!captured) {
                return false;
            }
        }
        return true;
    }

    void consumeTravelSupplies() {
        if (this.food > 0) {
            this.food -= 1;
        }
    }
}
