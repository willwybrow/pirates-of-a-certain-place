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
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.withHealth("Test Squid", 200));
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.EAST, 1L);

        // Spend the single starting cannon round so the weapon is now empty.
        sea.attemptToAttack(Weapon.CANNON, 2L);
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);

        int playerHealthBeforeAttack = sea.playerDetails().health();
        int opponentHealthBeforeAttack = opponentHealth(sea);

        sea.attemptToAttack(Weapon.CANNON, 3L);

        assertThat(sea.playerDetails().health()).isEqualTo(playerHealthBeforeAttack);
        assertThat(opponentHealth(sea)).isEqualTo(opponentHealthBeforeAttack);
        assertThat(sea.playerDetails().position()).isEqualTo(start);
        assertThat(sea.hasActiveCombat()).isTrue();
        assertThat(sea.recentLog().get(0)).isEqualTo("Out of ammunition for Cannon.");
    }

    @Test
    void playerStartsEachGameWithOneRoundOfEveryAmmunitionWeapon() {
        Sea sea = new Sea(new TileFactory());

        for (Weapon weapon : sea.playerDetails().wieldableWeapons()) {
            int ammunition = sea.playerDetails().ammunitionByWeapon().getOrDefault(weapon, 0);
            if (weapon.usesAmmunition()) {
                assertThat(ammunition).as("starting ammunition for %s", weapon).isEqualTo(1);
            } else {
                assertThat(ammunition).as("%s does not use ammunition", weapon).isEqualTo(0);
            }
        }
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
                    return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Ammo Squid", 100));
                }
                return EmptyTile.generate();
            }
        });

        // Starts with one cannon round; the island cache adds another, for two in total.
        sea.attemptToTravel(Direction.WEST, 1L);
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(2);

        sea.attemptToTravel(Direction.EAST, 2L);
        sea.attemptToTravel(Direction.EAST, 3L);

        // Spend both rounds; each landed attack damages the opponent and consumes ammunition.
        long timestamp = 4L;
        int opponentHealthBeforeBarrage = opponentHealth(sea);
        sea.attemptToAttack(Weapon.CANNON, timestamp++);
        sea.attemptToAttack(Weapon.CANNON, timestamp++);

        int opponentHealthAfterBarrage = opponentHealth(sea);
        assertThat(opponentHealthAfterBarrage).isLessThan(opponentHealthBeforeBarrage);
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);

        sea.attemptToAttack(Weapon.CANNON, timestamp);

        assertThat(opponentHealth(sea)).isEqualTo(opponentHealthAfterBarrage);
        assertThat(sea.recentLog().get(0)).isEqualTo("Out of ammunition for Cannon.");
        assertThat(sea.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);
    }

    private static int opponentHealth(Sea sea) {
        return sea.currentOpponentDetails()
            .orElseThrow(() -> new IllegalStateException("Expected active combat opponent"))
            .health();
    }
}
