package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaHazardTest {

    @Test
    void icebergRevealsImmediatelyThenDamagesPlayerAfterDelayAndDisappears() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, HazardTile.iceberg());
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);

        long now = 0L;
        sea.recalculateGameState(now);

        // Revealed immediately, but the hazard has not resolved before the delay elapses.
        assertThat(sea.whatsAt(destination).isSpied()).isTrue();
        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.playerDetails().health()).isEqualTo(100);

        sea.recalculateGameState(now + 2_000L);

        assertThat(sea.playerDetails().position()).isEqualTo(destination);
        assertThat(sea.playerDetails().health()).isEqualTo(80);
        assertThat(sea.whatsAt(destination).isCompleted())
            .as("iceberg should be spent after it is encountered")
            .isTrue();
        assertThat(sea.recentLog().get(0)).isEqualTo("An iceberg gouged the hull for 20 damage.");
    }

    @Test
    void whirlpoolStealsACapturedTreasureAndRehidesItUnderAnUnexploredTile() {
        Hex start = Hex.ORIGIN;
        Hex treasureHex = Direction.EAST.move(start);
        Hex whirlpoolHex = Direction.WEST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(treasureHex, TreasureTile.emeraldOfHope());
                generated.put(whirlpoolHex, HazardTile.whirlpool());
                return generated;
            }
        });
        sea.startNewGame(7L, 0L);

        // Capture the emerald first, then sail back to the origin.
        sea.attemptToTravel(Direction.EAST, 1L);
        sea.recalculateGameState(2_000L);
        assertThat(sea.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isTrue();
        assertThat(sea.playerDetails().position()).isEqualTo(treasureHex);

        sea.attemptToTravel(Direction.WEST, 3_000L);
        assertThat(sea.playerDetails().position()).isEqualTo(start);

        // Sail into the whirlpool, which steals and re-hides the emerald after the delay.
        sea.attemptToTravel(Direction.WEST, 4_000L);
        sea.recalculateGameState(4_000L);
        assertThat(sea.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isTrue();

        sea.recalculateGameState(6_000L);

        assertThat(sea.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE))
            .as("whirlpool should steal the captured treasure")
            .isFalse();
        assertThat(sea.recentLog().get(0))
            .isEqualTo("A whirlpool swallowed the " + Treasure.EMERALD_OF_HOPE.displayName() + " and hid it anew.");
        assertThat(sea.whatsAt(whirlpoolHex).isCompleted()).isTrue();
    }

    @Test
    void whirlpoolIsHarmlessWithoutAnyCapturedTreasure() {
        Hex start = Hex.ORIGIN;
        Hex whirlpoolHex = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(whirlpoolHex, HazardTile.whirlpool());
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);
        sea.recalculateGameState(2_000L);

        assertThat(sea.playerDetails().position()).isEqualTo(whirlpoolHex);
        assertThat(sea.playerDetails().health()).isEqualTo(100);
        assertThat(sea.recentLog().get(0)).isEqualTo("A whirlpool churned past, but you had no treasure to lose.");
        assertThat(sea.whatsAt(whirlpoolHex).isCompleted()).isTrue();
    }
}
