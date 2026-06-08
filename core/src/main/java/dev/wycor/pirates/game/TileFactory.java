package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Optional;
import java.util.Random;

/**
 * Creates the tile for a hex during world generation.
 *
 * <p>Tile contents are a pure function of the world-generation random stream (derived from the
 * game seed) and the treasures already placed elsewhere on the map. It does not depend on player
 * progress, so the world layout is fully reproducible from the seed alone.
 */
public class TileFactory {

    public SeaTile create(Hex hex, Random worldGenRandom, Collection<SeaTile> existingTiles) {
        EnumMap<Treasure, Boolean> unavailableTreasures = new EnumMap<>(Treasure.class);
        for (Treasure treasure : Treasure.values()) {
            unavailableTreasures.put(treasure, false);
        }

        for (SeaTile existingTile : existingTiles) {
            Optional<Treasure> pendingTreasure = SeaEvent.treasureFor(existingTile.pendingEvent());
            pendingTreasure.ifPresent(treasure -> unavailableTreasures.put(treasure, true));

            Optional<Treasure> completedTreasure = SeaEvent.treasureFor(existingTile.completedEvent());
            completedTreasure.ifPresent(treasure -> unavailableTreasures.put(treasure, true));
        }

        return SeaTile.random(unavailableTreasures, worldGenRandom);
    }
}
