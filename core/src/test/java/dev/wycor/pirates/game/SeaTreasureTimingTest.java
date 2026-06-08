package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaTreasureTimingTest {

    @Test
    void treasureTileRevealsImmediatelyAndCompletesMovementAfterDelay() {
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TreasureTile.emeraldOfHope());
                return generated;
            }
        });

        sea.attemptToTravel(direction, 1L);

        long now = 0L;
        sea.recalculateGameState(now);

        SeaTile destinationTile = sea.whatsAt(destination);
        assertThat(destinationTile.isSpied()).isTrue();
        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isFalse();

        sea.recalculateGameState(now + 2_000L);

        assertThat(sea.playerDetails().position()).isEqualTo(destination);
        assertThat(sea.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isTrue();
    }
}
