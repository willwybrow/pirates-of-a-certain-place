package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GameTreasureTimingTest {

    @Test
    void treasureTileRevealsImmediatelyAndCompletesMovementAfterDelay() {
        GameRandom gameRandom = new GameRandom(0L);
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Game game = new Game(gameRandom, new TileFactory(gameRandom) {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TreasureTile.emeraldOfHope());
                return new World(generated);
            }
        }).startNewGame(0L);

        game.attemptToTravel(direction, 1L);

        long now = 0L;
        game.recalculateGameState(now);

        SeaTile destinationTile = game.whatsAt(destination);
        assertThat(destinationTile.isSpied()).isTrue();
        assertThat(game.playerDetails().position()).isEqualTo(start);
        assertThat(game.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isFalse();

        game.recalculateGameState(now + 2_000L);

        assertThat(game.playerDetails().position()).isEqualTo(destination);
        assertThat(game.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isTrue();
    }
}
