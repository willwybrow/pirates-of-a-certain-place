package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TestTiles;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatFleeTest {

    @Test
    void fleeingKeepsDamageAlreadyDoneToPlayer() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.withHealth("Test Squid", 40)));
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);
        sea.attemptToAttack(2L);

        int healthAfterCombatRound = sea.playerDetails().health();
        assertThat(healthAfterCombatRound).isLessThan(100);

        sea.attemptToFlee(3L);

        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.playerDetails().health()).isEqualTo(healthAfterCombatRound);
    }

    @Test
    void fleeingResetsMonsterToItsOriginalStateForNextEncounter() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public Map<Hex, SeaTile> generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 9)));
                return generated;
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);
        sea.attemptToAttack(2L);
        assertThat(sea.hasActiveCombat()).isTrue();

        sea.attemptToFlee(3L);

        sea.attemptToTravel(Direction.EAST, 4L);
        sea.attemptToAttack(5L);

        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.hasActiveCombat())
            .as("monster should be reset after flee, so one attack is not enough to finish")
            .isTrue();
    }
}
