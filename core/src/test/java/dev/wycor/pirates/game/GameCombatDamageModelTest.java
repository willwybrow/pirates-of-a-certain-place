package dev.wycor.pirates.game;

import dev.wycor.pirates.game.monster.GiantSquid;
import dev.wycor.pirates.game.monster.PirateShip;
import dev.wycor.pirates.game.monster.Phoenix;
import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TestTiles;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class GameCombatDamageModelTest {

    @Test
    void superEffectiveWeaponDamageFallsWithinExpectedRange() {
        Game game = prepareSeaForCombat(SeaEvent.GIANT_SQUID,
            () -> GiantSquid.withHealth(GiantSquid.MIN_HEALTH));

        int opponentHealthBeforeAttack = opponentHealth(game);
        game.attemptToAttack(Weapon.HARPOON, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(game);

        assertThat(damageDone).isBetween(27, 33);
    }

    @Test
    void mediumEffectiveWeaponDamageFallsWithinExpectedRange() {
        Game game = prepareSeaForCombat(SeaEvent.GIANT_SQUID,
            () -> GiantSquid.withHealth(GiantSquid.MIN_HEALTH));

        int opponentHealthBeforeAttack = opponentHealth(game);
        game.attemptToAttack(Weapon.CANNON, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(game);

        assertThat(damageDone).isBetween(17, 23);
    }

    @Test
    void notVeryEffectiveWeaponDamageFallsWithinExpectedRange() {
        Game game = prepareSeaForCombat(SeaEvent.PHOENIX,
            () -> Phoenix.withHealth(Phoenix.MIN_HEALTH));

        int opponentHealthBeforeAttack = opponentHealth(game);
        game.attemptToAttack(Weapon.FLAMING_ARROWS, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(game);

        assertThat(damageDone).isBetween(2, 8);
    }

    @Test
    void cutlassDamageFallsWithinEmpiricalRange() {
        Game game = prepareSeaForCombat(SeaEvent.GIANT_SQUID,
            () -> GiantSquid.withHealth(GiantSquid.MIN_HEALTH));

        int opponentHealthBeforeAttack = opponentHealth(game);
        game.attemptToAttack(Weapon.CUTLASS, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(game);

        assertThat(damageDone).isBetween(6, 11);
    }

    @Test
    void monsterCounterattackDamageFallsWithinExpectedRange() {
        Game game = prepareSeaForCombat(SeaEvent.GIANT_SQUID,
            () -> GiantSquid.withHealth(GiantSquid.MIN_HEALTH));

        int playerHealthBeforeAttack = game.playerDetails().health();
        game.attemptToAttack(Weapon.CUTLASS, 4L);
        int damageTaken = playerHealthBeforeAttack - game.playerDetails().health();

        // The Giant Squid strikes with SQUID_STRIKE (base 5), so 5 +/- the 3-point combat variance.
        assertThat(damageTaken).isBetween(2, 8);
    }

    @Test
    void phoenixCounterattackDamageFallsWithinEmpiricalRange() {
        Game game = prepareSeaForCombat(SeaEvent.PHOENIX,
            () -> Phoenix.withHealth(Phoenix.MIN_HEALTH));

        int playerHealthBeforeAttack = game.playerDetails().health();
        game.attemptToAttack(Weapon.CUTLASS, 4L);
        int damageTaken = playerHealthBeforeAttack - game.playerDetails().health();

        assertThat(damageTaken).isBetween(8, 10);
    }

    @Test
    void pirateShipCounterattackDamageFallsWithinEmpiricalRange() {
        Game game = prepareSeaForCombat(SeaEvent.PIRATE_SHIP,
            () -> PirateShip.withHealth(PirateShip.MIN_HEALTH));

        int playerHealthBeforeAttack = game.playerDetails().health();
        game.attemptToAttack(Weapon.CUTLASS, 4L);
        int damageTaken = playerHealthBeforeAttack - game.playerDetails().health();

        assertThat(damageTaken).isBetween(4, 6);
    }

    private static Game prepareSeaForCombat(SeaEvent monsterEvent, Supplier<Monster> monsterSupplier) {
        Hex start = Hex.ORIGIN;
        Hex islandHex = Direction.WEST.move(start);
        Hex monsterHex = Direction.EAST.move(start);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(islandHex, IslandTile.generate());
                generated.put(monsterHex, TestTiles.testMonster(monsterEvent, false, monsterSupplier));
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.WEST, 1L);
        game.attemptToTravel(Direction.EAST, 2L);
        game.attemptToTravel(Direction.EAST, 3L);

        assertThat(game.hasActiveCombat()).isTrue();
        return game;
    }

    private static int opponentHealth(Game game) {
        return game.currentOpponentDetails()
            .orElseThrow(() -> new IllegalStateException("Expected active combat opponent"))
            .health();
    }
}
