package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Optional;

public class TileFactory {

    public SeaTile create(Hex hex, PlayerDetails playerDetails, Collection<SeaTile> existingTiles) {
        Objects.requireNonNull(playerDetails, "playerDetails");
        EnumMap<Treasure, Boolean> unavailableTreasures = new EnumMap<>(playerDetails.capturedTreasures());
        for (SeaTile existingTile : existingTiles) {
            Optional<Treasure> pendingTreasure = SeaEvent.treasureFor(existingTile.pendingEvent());
            pendingTreasure.ifPresent(treasure -> unavailableTreasures.put(treasure, true));

            Optional<Treasure> completedTreasure = SeaEvent.treasureFor(existingTile.completedEvent());
            completedTreasure.ifPresent(treasure -> unavailableTreasures.put(treasure, true));
        }

        return SeaTile.random(unavailableTreasures);
    }
}
