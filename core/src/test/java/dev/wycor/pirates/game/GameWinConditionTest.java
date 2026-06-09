package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameWinConditionTest {

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

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                for (Map.Entry<Hex, Treasure> entry : treasureByHex.entrySet()) {
                    generated.put(entry.getKey(), TreasureTile.forTreasure(entry.getValue()));
                }
                return new World(generated);
            }
        }.createGame();

        long timestamp = 1L;
        for (Direction direction : route) {
            game.attemptToTravel(direction, timestamp);
            game.recalculateGameState(timestamp + 2_000L);
            timestamp += 1L;
        }

        assertThat(game.playerDetails().capturedTreasures().values()).allMatch(Boolean::booleanValue);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.gameOverMessage()).isEqualTo("You found all the treasures!");
    }
}
