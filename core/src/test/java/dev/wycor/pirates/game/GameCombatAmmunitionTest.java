package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TestTiles;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class GameCombatAmmunitionTest {

    @Test
    void attackingWithAmmoWeaponAtZeroAmmunitionSkipsCombatRoundAndLogsIt() {
        Hex start = Hex.ORIGIN;
        Hex destination = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(destination, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.withHealth("Test Squid", 200)));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        // Spend the single starting cannon round so the weapon is now empty.
        game.attemptToAttack(Weapon.CANNON, 2L);
        assertThat(game.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);

        int playerHealthBeforeAttack = game.playerDetails().health();
        int opponentHealthBeforeAttack = opponentHealth(game);

        game.attemptToAttack(Weapon.CANNON, 3L);

        assertThat(game.playerDetails().health()).isEqualTo(playerHealthBeforeAttack);
        assertThat(opponentHealth(game)).isEqualTo(opponentHealthBeforeAttack);
        assertThat(game.playerDetails().position()).isEqualTo(start);
        assertThat(game.hasActiveCombat()).isTrue();
        assertThat(game.recentLog().get(0)).isEqualTo("Out of ammunition for Cannon.");
    }

    @Test
    void playerStartsEachGameWithOneRoundOfEveryAmmunitionWeapon() {
        Game game = TestGameFactory.createDefaultGame();

        for (Weapon weapon : game.playerDetails().wieldableWeapons()) {
            int ammunition = game.playerDetails().ammunitionByWeapon().getOrDefault(weapon, 0);
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

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(islandHex, IslandTile.generate());
                generated.put(monsterHex, TestTiles.testMonster(SeaEvent.GIANT_SQUID, false, () -> TestMonsters.harmless("Ammo Squid", 100)));
                return new World(generated);
            }
        }.createGame();

        // Starts with one cannon round; the island cache adds another, for two in total.
        game.attemptToTravel(Direction.WEST, 1L);
        assertThat(game.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(2);

        game.attemptToTravel(Direction.EAST, 2L);
        game.attemptToTravel(Direction.EAST, 3L);

        // Spend both rounds; each landed attack damages the opponent and consumes ammunition.
        long timestamp = 4L;
        int opponentHealthBeforeBarrage = opponentHealth(game);
        game.attemptToAttack(Weapon.CANNON, timestamp++);
        game.attemptToAttack(Weapon.CANNON, timestamp++);

        int opponentHealthAfterBarrage = opponentHealth(game);
        assertThat(opponentHealthAfterBarrage).isLessThan(opponentHealthBeforeBarrage);
        assertThat(game.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);

        game.attemptToAttack(Weapon.CANNON, timestamp);

        assertThat(opponentHealth(game)).isEqualTo(opponentHealthAfterBarrage);
        assertThat(game.recentLog().get(0)).isEqualTo("Out of ammunition for Cannon.");
        assertThat(game.playerDetails().ammunitionByWeapon().getOrDefault(Weapon.CANNON, 0)).isEqualTo(0);
    }

    private static int opponentHealth(Game game) {
        return game.currentOpponentDetails()
            .orElseThrow(() -> new IllegalStateException("Expected active combat opponent"))
            .health();
    }
}
