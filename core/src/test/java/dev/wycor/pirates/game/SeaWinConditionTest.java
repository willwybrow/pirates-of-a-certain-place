package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaWinConditionTest {

    @Test
    void collectingAllTreasuresEndsGameWithWinMessage() {
        Direction[] route = new Direction[] {
            Direction.EAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.NORTHEAST,
            Direction.NORTHEAST
        };

        Map<Hex, Treasure> treasureByHex = new HashMap<>();
        Hex cursor = Hex.ORIGIN;
        Treasure[] treasures = Treasure.values();
        for (int i = 0; i < treasures.length; i++) {
            cursor = route[i].move(cursor);
            treasureByHex.put(cursor, treasures[i]);
        }

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex, java.util.Random worldGenRandom, Collection<SeaTile> existingTiles) {
                Treasure treasure = treasureByHex.get(hex);
                if (treasure != null) {
                    return TreasureTile.forTreasure(treasure);
                }
                return EmptyTile.generate();
            }
        });

        long timestamp = 1L;
        for (Direction direction : route) {
            sea.attemptToTravel(direction, timestamp);
            sea.recalculateGameState(timestamp + 2_000L);
            timestamp += 1L;
        }

        assertThat(sea.playerDetails().capturedTreasures().values()).allMatch(Boolean::booleanValue);
        assertThat(sea.isGameOver()).isTrue();
        assertThat(sea.gameOverMessage()).isEqualTo("You found all the treasures!");
    }
}
