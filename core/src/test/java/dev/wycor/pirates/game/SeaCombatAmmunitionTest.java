package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatAmmunitionTest {

    @Test
    void attackingWithAmmoWeaponAtZeroAmmunitionSkipsCombatRoundAndLogsIt() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex, java.util.Random worldGenRandom, Collection<SeaTile> existingTiles) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> new Monster("Test Squid", 9, 3, 0) { });
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);

        int playerHealthBeforeAttack = sea.playerDetails().health();
        int opponentHealthBeforeAttack = opponentHealth(sea);

        sea.attemptToAttack(Weapon.CANNON, 2L);

        assertThat(sea.playerDetails().health()).isEqualTo(playerHealthBeforeAttack);
        assertThat(opponentHealth(sea)).isEqualTo(opponentHealthBeforeAttack);
        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.hasActiveCombat()).isTrue();
        assertThat(sea.recentLog().get(0)).isEqualTo("Out of ammunition for Cannon.");
    }

    @Test
    void ammunitionWeaponsConsumeAmmoAndLaterFailWhenEmpty() {
        Hex start = Hex.ORIGIN;
        Hex islandHex = Direction.WEST.move(start);
        Hex monsterHex = Direction.EAST.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex, java.util.Random worldGenRandom, Collection<SeaTile> existingTiles) {
                if (islandHex.equals(hex)) {
                    return IslandTile.generate();
                }
                if (monsterHex.equals(hex)) {
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> new Monster("Ammo Squid", 40, 0, 0) { });
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.WEST, 1L);
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(1);

        sea.attemptToTravel(Direction.EAST, 2L);
        sea.attemptToTravel(Direction.EAST, 3L);

        int opponentHealthBeforeFirstCannonAttack = opponentHealth(sea);
        sea.attemptToAttack(Weapon.CANNON, 4L);

        int opponentHealthAfterFirstCannonAttack = opponentHealth(sea);
        assertThat(opponentHealthAfterFirstCannonAttack).isLessThan(opponentHealthBeforeFirstCannonAttack);
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);

        sea.attemptToAttack(Weapon.CANNON, 5L);

        assertThat(opponentHealth(sea)).isEqualTo(opponentHealthAfterFirstCannonAttack);
        assertThat(sea.recentLog().get(0)).isEqualTo("Out of ammunition for Cannon.");
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);
    }

    private static int opponentHealth(Sea sea) {
        return sea.currentOpponentDetails()
            .orElseThrow(() -> new IllegalStateException("Expected active combat opponent"))
            .health();
    }
}
