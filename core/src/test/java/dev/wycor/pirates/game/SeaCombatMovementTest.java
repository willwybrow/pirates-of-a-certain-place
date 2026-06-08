package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TestTiles;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatMovementTest {

    @Test
    void playerMovesIntoDestinationImmediatelyWhenCombatEnds() {
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 1)));
                return generated;
            }
        });

        sea.attemptToTravel(direction, 1L);
        assertThat(sea.playerDetails().position()).isEqualTo(start);

        sea.attemptToAttack(2L);

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
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(east, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 9)));
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);

        assertThat(sea.hasActiveCombat()).isTrue();
        assertThat(sea.getPlayerDestination()).contains(east);

        sea.attemptToTravel(Direction.WEST, 2L);
        sea.attemptToAttack(3L);

        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.getPlayerDestination()).contains(east);

        long timestamp = 4L;
        while (sea.hasActiveCombat() && timestamp < 20L) {
            sea.attemptToAttack(timestamp);
            timestamp += 1L;
        }

        assertThat(sea.playerDetails().position()).isEqualTo(east);
        assertThat(sea.getPlayerDestination()).isEmpty();
    }

    @Test
    void dyingInCombatEndsGameAndPreventsFurtherMovement() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.withHealth("Fatal Squid", 100)));
                return generated;
            }
        });
        sea.startNewGame(42L, 0L);

        sea.attemptToTravel(Direction.EAST, 1L);

        // The monster out-damages the player over the fight, so trading blows ends in defeat.
        long timestamp = 2L;
        while (sea.hasActiveCombat() && !sea.isGameOver() && timestamp < 100L) {
            sea.attemptToAttack(timestamp++);
        }

        assertThat(sea.isGameOver()).isTrue();
        assertThat(sea.gameOverMessage()).isEqualTo("You have been defeated!");
        assertThat(sea.playerDetails().position()).isEqualTo(start);

        sea.attemptToTravel(Direction.EAST, timestamp);

        assertThat(sea.playerDetails().position()).isEqualTo(start);
    }
}
