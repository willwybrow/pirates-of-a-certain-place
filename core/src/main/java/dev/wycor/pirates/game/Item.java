package dev.wycor.pirates.game;

public enum Item {
    TAR("Tar"),
    MAP("Map"),
    SEXTANT("Sextant"),
    SPYGLASS("Spyglass");

    private final String displayName;

    Item(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return this.displayName;
    }
}
