package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeaFoodTest {

    @Test
    void playerStartsWithTwentyFood() {
        Sea sea = new Sea();

        assertThat(sea.playerFood()).isEqualTo(20);
    }

    @Test
    void eachVisitedTileConsumesOneFoodWhileFoodIsAvailable() {
        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.EAST);

        assertThat(sea.playerFood()).isEqualTo(19);
        assertThat(sea.playerHealth()).isEqualTo(20);
    }

    @Test
    void movingAtZeroFoodConsumesFiveHealthInstead() {
        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                return EmptyTile.generate();
            }
        });

        for (int i = 0; i < 20; i++) {
            sea.attemptToTravel(i % 2 == 0 ? Direction.EAST : Direction.WEST);
        }

        assertThat(sea.playerFood()).isEqualTo(0);
        assertThat(sea.playerHealth()).isEqualTo(20);

        sea.attemptToTravel(Direction.EAST);

        assertThat(sea.playerFood()).isEqualTo(0);
        assertThat(sea.playerHealth()).isEqualTo(15);
    }

    @Test
    void islandsResupplyTenFood() {
        Hex islandHex = Direction.EAST.move(Hex.ORIGIN);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (islandHex.equals(hex)) {
                    return IslandTile.generate();
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.EAST);

        assertThat(sea.playerFood()).isEqualTo(29);
    }
}
