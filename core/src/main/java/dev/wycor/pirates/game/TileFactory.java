package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.EnumMap;
import java.util.Map;

public class TileFactory {

    public SeaTile create(Hex hex) {
        return SeaTile.random();
    }

    public SeaTile create(Hex hex, Map<Treasure, Boolean> capturedTreasures) {
        if (this.getClass() == TileFactory.class) {
            return SeaTile.random(new EnumMap<>(capturedTreasures));
        }
        return create(hex);
    }
}
