package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatMovementTest {

    @Test
    void playerMovesIntoDestinationImmediatelyWhenCombatEnds() {
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> new Monster("Test Squid", 1, 0, 0) { });
                }
                return super.create(hex);
            }
        });

        sea.attemptToTravel(direction);
        assertThat(sea.playerDetails().position()).isEqualTo(start);

        sea.attemptToAttack();

        assertThat(sea.playerDetails().position())
            .as("player should enter destination as soon as killing blow ends combat")
            .isEqualTo(destination);
    }

    @Test
    void activeCombatKeepsOriginalCourseUntilCombatResolves() {
        Hex start = Hex.ORIGIN;
        Hex east = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (east.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> new Monster("Test Squid", 9, 0, 0) { });
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.EAST);

        assertThat(sea.hasActiveCombat()).isTrue();
        assertThat(sea.getPlayerDestination()).contains(east);

        sea.attemptToTravel(Direction.WEST);
        sea.attemptToAttack();

        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.getPlayerDestination()).contains(east);

        sea.attemptToAttack();

        assertThat(sea.playerDetails().position()).isEqualTo(east);
        assertThat(sea.getPlayerDestination()).isEmpty();
    }

    @Test
    void dyingInCombatEndsGameAndPreventsFurtherMovement() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> new Monster("Fatal Squid", 100, 30, 0) { });
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.EAST);
        sea.attemptToAttack();

        assertThat(sea.isGameOver()).isTrue();
        assertThat(sea.playerDetails().position()).isEqualTo(start);

        sea.attemptToTravel(Direction.EAST);

        assertThat(sea.playerDetails().position()).isEqualTo(start);
    }
}
