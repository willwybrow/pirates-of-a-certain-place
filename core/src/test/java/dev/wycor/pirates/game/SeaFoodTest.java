package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaFoodTest {

    @Test
    void playerStartsWithTwentyFood() {
        Sea sea = new Sea(new TileFactory());

        assertThat(sea.playerDetails().food()).isEqualTo(20);
    }

    @Test
    void eachVisitedTileConsumesOneFoodWhileFoodIsAvailable() {
        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                return new HashMap<>();
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);

        assertThat(sea.playerDetails().food()).isEqualTo(19);
        assertThat(sea.playerDetails().health()).isEqualTo(100);
    }

    @Test
    void reachingZeroFoodEndsTheGame() {
        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                return new HashMap<>();
            }
        });

        for (int i = 0; i < 20; i++) {
            sea.attemptToTravel(i % 2 == 0 ? Direction.EAST : Direction.WEST, i + 1L);
        }

        assertThat(sea.playerDetails().food()).isEqualTo(0);
        assertThat(sea.playerDetails().health()).isEqualTo(100);
        assertThat(sea.isGameOver()).isTrue();
        assertThat(sea.gameOverMessage()).isEqualTo("Your crew has starved!");

        Hex positionAtStarvation = sea.playerDetails().position();
        sea.attemptToTravel(Direction.EAST, 21L);

        assertThat(sea.playerDetails().food()).isEqualTo(0);
        assertThat(sea.playerDetails().health()).isEqualTo(100);
        assertThat(sea.playerDetails().position()).isEqualTo(positionAtStarvation);
    }

    @Test
    void islandsResupplyTenFood() {
        Hex islandHex = Direction.EAST.move(Hex.ORIGIN);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(islandHex, IslandTile.generate());
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);

        assertThat(sea.playerDetails().food()).isEqualTo(29);
    }

    @Test
    void islandRewardsAreGrantedOnlyOncePerTile() {
        Hex islandHex = Direction.EAST.move(Hex.ORIGIN);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(islandHex, IslandTile.generate());
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);
        sea.attemptToTravel(Direction.WEST, 2L);
        sea.attemptToTravel(Direction.EAST, 3L);

        assertThat(sea.playerDetails().food()).isEqualTo(27);
    }
}
