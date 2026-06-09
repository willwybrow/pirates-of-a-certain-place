package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TestTiles;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class GameCombatMovementTest {

    @Test
    void playerMovesIntoDestinationImmediatelyWhenCombatEnds() {
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 1)));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(direction, 1L);
        assertThat(game.playerDetails().position()).isEqualTo(start);

        game.attemptToAttack(2L);

        assertThat(game.playerDetails().position())
            .as("player should enter destination as soon as killing blow ends combat")
            .isEqualTo(destination);
    }

    @Test
    void activeCombatKeepsOriginalCourseUntilCombatResolves() {
        Hex start = Hex.ORIGIN;
        Hex east = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(east, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 20)));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        assertThat(game.hasActiveCombat()).isTrue();
        assertThat(game.getPlayerDestination()).contains(east);

        game.attemptToTravel(Direction.WEST, 2L);
        game.attemptToAttack(3L);

        assertThat(game.playerDetails().position()).isEqualTo(start);
        assertThat(game.getPlayerDestination()).contains(east);

        long timestamp = 4L;
        while (game.hasActiveCombat() && timestamp < 20L) {
            game.attemptToAttack(timestamp);
            timestamp += 1L;
        }

        assertThat(game.playerDetails().position()).isEqualTo(east);
        assertThat(game.getPlayerDestination()).isEmpty();
    }

    @Test
    void dyingInCombatEndsGameAndPreventsFurtherMovement() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false,
                    () -> TestMonsters.withHealthAndStrike("Fatal Squid", 170, Weapon.PHOENIX_STRIKE)));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        // The monster out-damages the player over the fight, so trading blows ends in defeat.
        long timestamp = 2L;
        while (game.hasActiveCombat() && !game.isGameOver() && timestamp < 100L) {
            game.attemptToAttack(timestamp++);
        }

        assertThat(game.isGameOver()).isTrue();
        assertThat(game.gameOverMessage()).isEqualTo("You have been defeated!");
        assertThat(game.playerDetails().position()).isEqualTo(start);

        game.attemptToTravel(Direction.EAST, timestamp);

        assertThat(game.playerDetails().position()).isEqualTo(start);
    }
}
