package dev.wycor.pirates.game;

/**
 * Anything that can deal damage in combat, including both the weapons the player wields and the
 * natural "strikes" that monsters attack with.
 *
 * <p>Each weapon has a base damage and may require ammunition. A weapon does not know who can wield
 * it; that is declared by each {@link Combatant} via its wieldable weapons.
 */
public enum Weapon {
    // Player weapons.
    CUTLASS("Cutlass", 10, false, -4, 1),
    CANNON("Cannon", 10, true),
    GIANT_AXE("Giant Axe", 10, true),
    FLAMING_ARROWS("Flaming Arrows", 10, true),
    HARPOON("Harpoon", 10, true),
    ICE_DAGGERS("Ice Daggers", 10, true),

    // Monster strikes: never use ammunition.
    SQUID_STRIKE("Tentacle Strike", 5, false),
    SEAWEED_STRIKE("Seaweed Lash", 10, false),
    PHOENIX_STRIKE("Phoenix Flames", 9, false, -1, 1),
    GHOST_STRIKE("Spectral Strike", 10, false),
    PIRATE_STRIKE("Pirate Volley", 5, false, -1, 1);

    private static final int DEFAULT_MIN_VARIANCE = -3;
    private static final int DEFAULT_MAX_VARIANCE = 3;

    private final String displayName;
    private final int baseDamage;
    private final boolean usesAmmunition;
    private final int minVariance;
    private final int maxVariance;

    Weapon(String displayName, int baseDamage, boolean usesAmmunition) {
        this(displayName, baseDamage, usesAmmunition, DEFAULT_MIN_VARIANCE, DEFAULT_MAX_VARIANCE);
    }

    Weapon(String displayName, int baseDamage, boolean usesAmmunition, int minVariance, int maxVariance) {
        this.displayName = displayName;
        this.baseDamage = baseDamage;
        this.usesAmmunition = usesAmmunition;
        this.minVariance = minVariance;
        this.maxVariance = maxVariance;
    }

    public String displayName() {
        return this.displayName;
    }

    public int baseDamage() {
        return this.baseDamage;
    }

    public boolean usesAmmunition() {
        return this.usesAmmunition;
    }

    public int minVariance() {
        return this.minVariance;
    }

    public int maxVariance() {
        return this.maxVariance;
    }
}
