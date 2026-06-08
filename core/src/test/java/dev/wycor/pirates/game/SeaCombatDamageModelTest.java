package dev.wycor.pirates.game;

import dev.wycor.pirates.game.monster.GiantSquid;
import dev.wycor.pirates.game.monster.Phoenix;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatDamageModelTest {

    @Test
    void superEffectiveWeaponDamageFallsWithinExpectedRange() {
        Sea sea = prepareSeaForCombat(SeaEvent.GIANT_SQUID, GiantSquid::new);

        int opponentHealthBeforeAttack = opponentHealth(sea);
        sea.attemptToAttack(Weapon.HARPOON, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(sea);

        assertThat(damageDone).isBetween(27, 33);
    }

    @Test
    void mediumEffectiveWeaponDamageFallsWithinExpectedRange() {
        Sea sea = prepareSeaForCombat(SeaEvent.GIANT_SQUID, GiantSquid::new);

        int opponentHealthBeforeAttack = opponentHealth(sea);
        sea.attemptToAttack(Weapon.CANNON, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(sea);

        assertThat(damageDone).isBetween(17, 23);
    }

    @Test
    void notVeryEffectiveWeaponDamageFallsWithinExpectedRange() {
        Sea sea = prepareSeaForCombat(SeaEvent.PHOENIX, Phoenix::new);

        int opponentHealthBeforeAttack = opponentHealth(sea);
        sea.attemptToAttack(Weapon.FLAMING_ARROWS, 4L);
        int damageDone = opponentHealthBeforeAttack - opponentHealth(sea);

        assertThat(damageDone).isBetween(2, 8);
    }

    @Test
    void monsterCounterattackDamageFallsWithinExpectedRange() {
        Sea sea = prepareSeaForCombat(SeaEvent.GIANT_SQUID, GiantSquid::new);

        int playerHealthBeforeAttack = sea.playerDetails().health();
        sea.attemptToAttack(Weapon.CUTLASS, 4L);
        int damageTaken = playerHealthBeforeAttack - sea.playerDetails().health();

        assertThat(damageTaken).isBetween(7, 13);
    }

    private static Sea prepareSeaForCombat(SeaEvent monsterEvent, Supplier<Monster> monsterSupplier) {
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
                    return new MonsterTile(monsterEvent, false, monsterSupplier);
                }
                return EmptyTile.generate();
            }
        });

        sea.attemptToTravel(Direction.WEST, 1L);
        sea.attemptToTravel(Direction.EAST, 2L);
        sea.attemptToTravel(Direction.EAST, 3L);

        assertThat(sea.hasActiveCombat()).isTrue();
        return sea;
    }

    private static int opponentHealth(Sea sea) {
        return sea.currentOpponentDetails()
            .orElseThrow(() -> new IllegalStateException("Expected active combat opponent"))
            .health();
    }
}
