package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Balance probes: each monster should be an even (roughly 50/50) fight for a player who only swings
 * the Cutlass. Every probe runs a thousand independent, fully-deterministic combats (one per seed)
 * and asserts the player's win rate lands within 49%&ndash;51%.
 *
 * <p>These tests are intentionally strict and may currently fail; they exist to drive balance tweaks.
 */
class MonsterBalanceTest {

    private static final int COMBAT_SAMPLES = 1_000;
    private static final double MIN_WIN_RATE = 0.49;
    private static final double MAX_WIN_RATE = 0.51;

    @Test
    void giantSquidIsAnEvenFightWithTheCutlass() {
        assertWinRateIsBalanced(MonsterTile::giantSquid);
    }

    @Test
    void seaweedMonsterIsAnEvenFightWithTheCutlass() {
        assertWinRateIsBalanced(MonsterTile::seaweedMonster);
    }

    @Test
    void phoenixIsAnEvenFightWithTheCutlass() {
        assertWinRateIsBalanced(MonsterTile::phoenix);
    }

    @Test
    void ghostShipIsAnEvenFightWithTheCutlass() {
        assertWinRateIsBalanced(MonsterTile::ghostShip);
    }

    @Test
    void pirateShipIsAnEvenFightWithTheCutlass() {
        assertWinRateIsBalanced(MonsterTile::pirateShip);
    }

    private static void assertWinRateIsBalanced(Supplier<MonsterTile> monsterTileSupplier) {
        int playerWins = 0;
        for (int seed = 0; seed < COMBAT_SAMPLES; seed++) {
            if (playerWinsCombat(monsterTileSupplier, seed)) {
                playerWins++;
            }
        }

        double winRate = (double) playerWins / COMBAT_SAMPLES;
        assertThat(winRate)
            .as("player cutlass win rate over %d combats", COMBAT_SAMPLES)
            .isBetween(MIN_WIN_RATE, MAX_WIN_RATE);
    }

    /**
     * Runs a single combat to its conclusion with a fixed seed: the player sails into the monster and
     * keeps swinging the Cutlass until either the monster dies (player wins) or the player dies.
     */
    private static boolean playerWinsCombat(Supplier<MonsterTile> monsterTileSupplier, long seed) {
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
        while (sea.hasActiveCombat() && !sea.isGameOver()) {
            sea.attemptToAttack(Weapon.CUTLASS, timestamp++);
        }

        // The player wins if the monster is gone and the player is still standing.
        return !sea.isGameOver();
    }
}
