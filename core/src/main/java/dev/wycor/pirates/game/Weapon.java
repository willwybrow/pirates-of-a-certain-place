package dev.wycor.pirates.game;

public enum Weapon {
    CUTLASS("Cutlass", false),
    CANNON("Cannon", true),
    GIANT_AXE("Giant Axe", true),
    FLAMING_ARROWS("Flaming Arrows", true),
    HARPOON("Harpoon", true),
    ICE_DAGGERS("Ice Daggers", true);

    private final String displayName;
    private final boolean usesAmmunition;

    Weapon(String displayName, boolean usesAmmunition) {
        this.displayName = displayName;
        this.usesAmmunition = usesAmmunition;
    }

    public String displayName() {
        return this.displayName;
    }

    public boolean usesAmmunition() {
        return this.usesAmmunition;
    }
}
