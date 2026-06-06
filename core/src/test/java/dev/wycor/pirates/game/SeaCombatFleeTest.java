package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatFleeTest {

    @Test
    void fleeingKeepsDamageAlreadyDoneToPlayer() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.KRAKEN, false, () -> new Monster("Test Kraken", 10, 3, 0));
                }
                return super.create(hex);
            }
        });

        sea.attemptToTravel(Direction.EAST);
        sea.attemptToAttack();

        int healthAfterCombatRound = sea.playerDetails.health;
        assertThat(healthAfterCombatRound).isLessThan(20);

        sea.attemptToFlee();

        assertThat(sea.playerPosition()).isEqualTo(start);
        assertThat(sea.playerDetails.health).isEqualTo(healthAfterCombatRound);
    }

    @Test
    void fleeingResetsMonsterToItsOriginalStateForNextEncounter() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.KRAKEN, false, () -> new Monster("Test Kraken", 9, 0, 0));
                }
                return super.create(hex);
            }
        });

        sea.attemptToTravel(Direction.EAST);
        sea.attemptToAttack();
        assertThat(sea.hasActiveCombat()).isTrue();

        sea.attemptToFlee();

        sea.attemptToTravel(Direction.EAST);
        sea.attemptToAttack();

        assertThat(sea.playerPosition()).isEqualTo(start);
        assertThat(sea.hasActiveCombat())
            .as("monster should be reset after flee, so one attack is not enough to finish")
            .isTrue();
    }
}
