package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TestTiles;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class GameCombatFleeTest {

    @Test
    void fleeingKeepsDamageAlreadyDoneToPlayer() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.withHealth("Test Squid", 40)));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);
        game.attemptToAttack(2L);

        int healthAfterCombatRound = game.playerDetails().health();
        assertThat(healthAfterCombatRound).isLessThan(100);

        game.attemptToFlee(3L);

        assertThat(game.playerDetails().position()).isEqualTo(start);
        assertThat(game.playerDetails().health()).isEqualTo(healthAfterCombatRound);
    }

    @Test
    void fleeingResetsMonsterToItsOriginalStateForNextEncounter() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 9)));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);
        game.attemptToAttack(2L);
        assertThat(game.hasActiveCombat()).isTrue();

        game.attemptToFlee(3L);

        game.attemptToTravel(Direction.EAST, 4L);
        game.attemptToAttack(5L);

        assertThat(game.playerDetails().position()).isEqualTo(start);
        assertThat(game.hasActiveCombat())
            .as("monster should be reset after flee, so one attack is not enough to finish")
            .isTrue();
    }
}
