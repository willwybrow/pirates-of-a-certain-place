package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class GameFoodTest {

    @Test
    void playerStartsWithTwentyFood() {
        Game game = TestGameFactory.createDefaultGame();

        assertThat(game.playerDetails().food()).isEqualTo(20);
    }

    @Test
    void eachVisitedTileConsumesOneFoodWhileFoodIsAvailable() {
        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                return new World(new HashMap<>());
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        assertThat(game.playerDetails().food()).isEqualTo(19);
        assertThat(game.playerDetails().health()).isEqualTo(100);
    }

    @Test
    void reachingZeroFoodEndsTheGame() {
        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                return new World(new HashMap<>());
            }
        }.createGame();

        for (int i = 0; i < 20; i++) {
            game.attemptToTravel(i % 2 == 0 ? Direction.EAST : Direction.WEST, i + 1L);
        }

        assertThat(game.playerDetails().food()).isEqualTo(0);
        assertThat(game.playerDetails().health()).isEqualTo(100);
        assertThat(game.isGameOver()).isTrue();
        assertThat(game.gameOverMessage()).isEqualTo("Your crew has starved!");

        Hex positionAtStarvation = game.playerDetails().position();
        game.attemptToTravel(Direction.EAST, 21L);

        assertThat(game.playerDetails().food()).isEqualTo(0);
        assertThat(game.playerDetails().health()).isEqualTo(100);
        assertThat(game.playerDetails().position()).isEqualTo(positionAtStarvation);
    }

    @Test
    void islandsResupplyTenFood() {
        Hex islandHex = Direction.EAST.move(Hex.ORIGIN);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(islandHex, IslandTile.generate());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        assertThat(game.playerDetails().food()).isEqualTo(29);
    }

    @Test
    void islandRewardsAreGrantedOnlyOncePerTile() {
        Hex islandHex = Direction.EAST.move(Hex.ORIGIN);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(islandHex, IslandTile.generate());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);
        game.attemptToTravel(Direction.WEST, 2L);
        game.attemptToTravel(Direction.EAST, 3L);

        assertThat(game.playerDetails().food()).isEqualTo(27);
    }
}
