package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

public class TileFactory {

    public SeaTile create(Hex hex) {
        return SeaTile.random();
    }
}
