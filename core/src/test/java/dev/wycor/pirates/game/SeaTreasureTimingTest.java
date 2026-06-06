package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class SeaTreasureTimingTest {

    @Test
    void treasureTileRevealsImmediatelyAndCompletesMovementAfterDelay() {
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex, PlayerDetails playerDetails, Collection<SeaTile> existingTiles) {
                if (destination.equals(hex)) {
                    return TreasureTile.emeraldOfHope();
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(direction);

        long now = System.currentTimeMillis();
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
