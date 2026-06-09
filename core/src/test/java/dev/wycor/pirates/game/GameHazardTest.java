package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class GameHazardTest {

    @Test
    void icebergRevealsImmediatelyThenDamagesPlayerAfterDelayAndDisappears() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, HazardTile.iceberg());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        long now = 0L;
        game.recalculateGameState(now);

        // Revealed immediately, but the hazard has not resolved before the delay elapses.
        assertThat(game.whatsAt(destination).isSpied()).isTrue();
        assertThat(game.playerDetails().position()).isEqualTo(start);
        assertThat(game.playerDetails().health()).isEqualTo(100);

        game.recalculateGameState(now + 2_000L);

        assertThat(game.playerDetails().position()).isEqualTo(destination);
        assertThat(game.playerDetails().health()).isEqualTo(80);
        assertThat(game.whatsAt(destination).isCompleted())
            .as("iceberg should be spent after it is encountered")
            .isTrue();
        assertThat(game.recentLog().get(0)).isEqualTo("An iceberg gouged the hull for 20 damage.");
    }

    @Test
    void whirlpoolStealsACapturedTreasureAndRehidesItUnderAnUnexploredTile() {
        Hex start = Hex.ORIGIN;
        Hex treasureHex = Direction.EAST.move(start);
        Hex whirlpoolHex = Direction.WEST.move(start);
        Hex rehideHex = Direction.NORTHWEST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(treasureHex, TreasureTile.emeraldOfHope());
                generated.put(whirlpoolHex, HazardTile.whirlpool());
                generated.put(rehideHex, EmptyTile.generate());
                return new World(generated);
            }
        }.createGame();

        // Capture the emerald first, then sail back to the origin.
        game.attemptToTravel(Direction.EAST, 1L);
        game.recalculateGameState(2_000L);
        assertThat(game.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isTrue();
        assertThat(game.playerDetails().position()).isEqualTo(treasureHex);

        game.attemptToTravel(Direction.WEST, 3_000L);
        assertThat(game.playerDetails().position()).isEqualTo(start);

        // Sail into the whirlpool, which steals and re-hides the emerald after the delay.
        game.attemptToTravel(Direction.WEST, 4_000L);
        game.recalculateGameState(4_000L);
        assertThat(game.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE)).isTrue();

        game.recalculateGameState(6_000L);

        assertThat(game.playerDetails().capturedTreasures().get(Treasure.EMERALD_OF_HOPE))
            .as("whirlpool should steal the captured treasure")
            .isFalse();
        assertThat(game.recentLog().get(0))
            .isEqualTo("A whirlpool swallowed the " + Treasure.EMERALD_OF_HOPE.displayName() + " and hid it anew.");
        assertThat(game.whatsAt(whirlpoolHex).isCompleted()).isTrue();
    }

    @Test
    void whirlpoolIsHarmlessWithoutAnyCapturedTreasure() {
        Hex start = Hex.ORIGIN;
        Hex whirlpoolHex = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(whirlpoolHex, HazardTile.whirlpool());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);
        game.recalculateGameState(2_000L);

        assertThat(game.playerDetails().position()).isEqualTo(whirlpoolHex);
        assertThat(game.playerDetails().health()).isEqualTo(100);
        assertThat(game.recentLog().get(0)).isEqualTo("A whirlpool churned past, but you had no treasure to lose.");
        assertThat(game.whatsAt(whirlpoolHex).isCompleted()).isTrue();
    }
}
