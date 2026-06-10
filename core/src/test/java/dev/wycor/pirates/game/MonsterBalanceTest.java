package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.MonsterTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;
import java.util.function.Function;

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

    private static void assertCutlassWinRateIsBalanced(Function<Random, MonsterTile> monsterTileFactory) {
        double winRate = measureWinRate(monsterTileFactory, null);
        assertThat(winRate)
            .as("player cutlass win rate over %d combats", COMBAT_SAMPLES)
            .isBetween(CUTLASS_MIN_WIN_RATE, CUTLASS_MAX_WIN_RATE);
    }

    private static void assertSuperEffectiveOpeningWinRate(Function<Random, MonsterTile> monsterTileFactory,
                                                           Weapon superEffectiveWeapon) {
        double winRate = measureWinRate(monsterTileFactory, superEffectiveWeapon);
        assertThat(winRate)
            .as("player win rate with an opening %s over %d combats", superEffectiveWeapon, COMBAT_SAMPLES)
            .isBetween(SUPER_EFFECTIVE_MIN_WIN_RATE, SUPER_EFFECTIVE_MAX_WIN_RATE);
    }

    private static double measureWinRate(Function<Random, MonsterTile> monsterTileFactory, Weapon openingWeapon) {
        int playerWins = 0;
        for (int seed = 0; seed < COMBAT_SAMPLES; seed++) {
            if (playerWinsCombat(monsterTileFactory, seed, openingWeapon)) {
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
    private static boolean playerWinsCombat(Function<Random, MonsterTile> monsterTileFactory, long seed,
                                            Weapon openingWeapon) {
        Hex monsterHex = Direction.EAST.move(Hex.ORIGIN);

        GameRandom gameRandom = new GameRandom(seed);
        TileFactory tileFactory = new TileFactory(gameRandom.world(), gameRandom.monster()) {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(monsterHex, monsterTileFactory.apply(gameRandom.monster()));
                return new World(generated);
            }
        };
        AttackResolver attackResolver = new AttackResolver(gameRandom.monster());
        HazardEngine hazardEngine = new HazardEngine(gameRandom.hazard());
        ItemEngine itemEngine = new ItemEngine();
        Game game = new Game(tileFactory, attackResolver, hazardEngine, itemEngine);
        game.startNewGame(0L);

        game.attemptToTravel(Direction.EAST, 1L);

        long timestamp = 2L;
        if (openingWeapon != null && game.hasActiveCombat() && !game.isGameOver()) {
            game.attemptToAttack(openingWeapon, timestamp++);
        }

        while (game.hasActiveCombat() && !game.isGameOver()) {
            game.attemptToAttack(Weapon.CUTLASS, timestamp++);
        }

        // The player wins if the monster is gone and the player is still standing.
        return !game.isGameOver();
    }
}
