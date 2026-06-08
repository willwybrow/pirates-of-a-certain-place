package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatFleeTest {

    @Test
    void fleeingKeepsDamageAlreadyDoneToPlayer() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex, java.util.Random worldGenRandom, Collection<SeaTile> existingTiles) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.withHealth("Test Squid", 40));
                }
                return EmptyTile.generate();
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
            public SeaTile create(Hex hex, java.util.Random worldGenRandom, Collection<SeaTile> existingTiles) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Test Squid", 9));
                }
                return EmptyTile.generate();
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
