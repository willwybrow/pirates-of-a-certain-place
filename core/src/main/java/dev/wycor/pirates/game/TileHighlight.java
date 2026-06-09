package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

public final class TileHighlight {
    public enum Color {
        GREEN,
        RED
    }

    private final Hex hex;
    private final Color color;

    public TileHighlight(Hex hex, Color color) {
        this.hex = hex;
        this.color = color;
    }

    public Hex hex() {
        return this.hex;
    }

    public Color color() {
        return this.color;
    }
}
