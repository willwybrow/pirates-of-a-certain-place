package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Balance probes anchored on two constraint points for a player fighting each monster:
 *
 * <ul>
 *   <li><b>Cutlass only:</b> the fight should be roughly even &mdash; win rate within 49%&ndash;51%.</li>
 *   <li><b>One opening super-effective round, then Cutlass:</b> the advantage should swing the fight
 *       in the player's favour &mdash; win rate within 74%&ndash;76%.</li>
 * </ul>
 *
 * <p>Each probe runs a thousand independent, fully-deterministic combats (one per seed). These tests
 * are intentionally strict and may currently fail; they exist to drive balance tweaks.
 */
@Disabled("Pending balance design work")
class MonsterBalanceTest {

    private static final int COMBAT_SAMPLES = 1_000;

    private static final double CUTLASS_MIN_WIN_RATE = 0.49;
    private static final double CUTLASS_MAX_WIN_RATE = 0.51;

    private static final double SUPER_EFFECTIVE_MIN_WIN_RATE = 0.74;
    private static final double SUPER_EFFECTIVE_MAX_WIN_RATE = 0.76;

    // --- Cutlass-only: each monster should be an even fight ---

    @Test
    void giantSquidIsAnEvenFightWithTheCutlass() {
        assertCutlassWinRateIsBalanced(MonsterTile::giantSquid);
    }

    @Test
    void seaweedMonsterIsAnEvenFightWithTheCutlass() {
        assertCutlassWinRateIsBalanced(MonsterTile::seaweedMonster);
    }

    @Test
    void phoenixIsAnEvenFightWithTheCutlass() {
        assertCutlassWinRateIsBalanced(MonsterTile::phoenix);
    }

    @Test
    void ghostShipIsAnEvenFightWithTheCutlass() {
        assertCutlassWinRateIsBalanced(MonsterTile::ghostShip);
    }

    @Test
    void pirateShipIsAnEvenFightWithTheCutlass() {
        assertCutlassWinRateIsBalanced(MonsterTile::pirateShip);
    }

    // --- One opening super-effective round, then Cutlass: the player should be favoured ---

    @Test
    void giantSquidFavoursPlayerWithAnOpeningHarpoon() {
        assertSuperEffectiveOpeningWinRate(MonsterTile::giantSquid, Weapon.HARPOON);
    }

    @Test
    void seaweedMonsterFavoursPlayerWithAnOpeningGiantAxe() {
        assertSuperEffectiveOpeningWinRate(MonsterTile::seaweedMonster, Weapon.GIANT_AXE);
    }

    @Test
    void phoenixFavoursPlayerWithAnOpeningIceDaggers() {
        assertSuperEffectiveOpeningWinRate(MonsterTile::phoenix, Weapon.ICE_DAGGERS);
    }

    @Test
    void ghostShipFavoursPlayerWithAnOpeningFlamingArrows() {
        assertSuperEffectiveOpeningWinRate(MonsterTile::ghostShip, Weapon.FLAMING_ARROWS);
    }

    @Test
    void pirateShipFavoursPlayerWithAnOpeningCannon() {
        assertSuperEffectiveOpeningWinRate(MonsterTile::pirateShip, Weapon.CANNON);
    }

    private static void assertCutlassWinRateIsBalanced(Supplier<MonsterTile> monsterTileSupplier) {
        double winRate = measureWinRate(monsterTileSupplier, null);
        assertThat(winRate)
            .as("player cutlass win rate over %d combats", COMBAT_SAMPLES)
            .isBetween(CUTLASS_MIN_WIN_RATE, CUTLASS_MAX_WIN_RATE);
    }

    private static void assertSuperEffectiveOpeningWinRate(Supplier<MonsterTile> monsterTileSupplier,
                                                           Weapon superEffectiveWeapon) {
        double winRate = measureWinRate(monsterTileSupplier, superEffectiveWeapon);
        assertThat(winRate)
            .as("player win rate with an opening %s over %d combats", superEffectiveWeapon, COMBAT_SAMPLES)
            .isBetween(SUPER_EFFECTIVE_MIN_WIN_RATE, SUPER_EFFECTIVE_MAX_WIN_RATE);
    }

    private static double measureWinRate(Supplier<MonsterTile> monsterTileSupplier, Weapon openingWeapon) {
        int playerWins = 0;
        for (int seed = 0; seed < COMBAT_SAMPLES; seed++) {
            if (playerWinsCombat(monsterTileSupplier, seed, openingWeapon)) {
                playerWins++;
            }
        }
        return (double) playerWins / COMBAT_SAMPLES;
    }

    /**
     * Runs a single combat to its conclusion with a fixed seed. If an {@code openingWeapon} is given,
     * the player fires one round of it first; thereafter (and for the whole fight otherwise) the player
     * keeps swinging the Cutlass until either the monster dies (player wins) or the player dies.
     */
    private static boolean playerWinsCombat(Supplier<MonsterTile> monsterTileSupplier, long seed,
                                            Weapon openingWeapon) {
        Hex monsterHex = Direction.EAST.move(Hex.ORIGIN);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex, java.util.Random worldGenRandom, Collection<SeaTile> existingTiles) {
                if (monsterHex.equals(hex)) {
                    return monsterTileSupplier.get();
                }
                return EmptyTile.generate();
            }
        });
        sea.startNewGame(seed, 0L);

        sea.attemptToTravel(Direction.EAST, 1L);

        long timestamp = 2L;
        if (openingWeapon != null && sea.hasActiveCombat() && !sea.isGameOver()) {
            sea.attemptToAttack(openingWeapon, timestamp++);
        }

        while (sea.hasActiveCombat() && !sea.isGameOver()) {
            sea.attemptToAttack(Weapon.CUTLASS, timestamp++);
        }

        // The player wins if the monster is gone and the player is still standing.
        return !sea.isGameOver();
    }
}
