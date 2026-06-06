package dev.wycor.pirates.game;

public enum Weapon {
    CUTLASS(false),
    CANNON(true),
    GIANT_AXE(true),
    FLAMING_ARROWS(true),
    HARPOON(true),
    ICE_DAGGERS(true);

    private final boolean usesAmmunition;

    Weapon(boolean usesAmmunition) {
        this.usesAmmunition = usesAmmunition;
    }

    public boolean usesAmmunition() {
        return this.usesAmmunition;
    }
}
