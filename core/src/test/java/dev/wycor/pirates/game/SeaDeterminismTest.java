package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeaDeterminismTest {

    private static final Direction[] SCRIPT = {
        Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.WEST,
        Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.EAST
    };

    @Test
    void sameSeedAndInputsProduceIdenticalObservableState() {
        Sea first = playScript(123456789L);
        Sea second = playScript(123456789L);

        assertThat(snapshot(second)).isEqualTo(snapshot(first));
    }

    @Test
    void differentSeedsDivergeForTheSameInputs() {
        Sea first = playScript(1L);
        Sea second = playScript(2L);

        assertThat(worldSnapshot(second)).isNotEqualTo(worldSnapshot(first));
    }

    @Test
    void incrementalCacheMatchesFullReplayFromScratch() {
        long seed = 987654321L;
        Sea sea = new Sea(new TileFactory());
        sea.startNewGame(seed, 0L);

        long timestamp = 1L;
        List<GameInput> playedInputs = new ArrayList<>();
        for (Direction direction : SCRIPT) {
            sea.attemptToTravel(direction, timestamp);
            // Attack whatever might be in the way so combat RNG is exercised too.
            sea.attemptToAttack(Weapon.CUTLASS, timestamp);
            timestamp += 1L;
        }
        playedInputs.addAll(sea.inputs());

        // The incrementally-maintained cache must equal a from-scratch replay of the same log.
        GameEngine engine = new GameEngine(new TileFactory());
        GameState replayed = engine.replay(seed, playedInputs, timestamp);

        assertThat(stateSnapshot(replayed)).isEqualTo(seaStateSnapshot(sea));
    }

    private static Sea playScript(long seed) {
        Sea sea = new Sea(new TileFactory());
        sea.startNewGame(seed, 0L);

        long timestamp = 1L;
        for (Direction direction : SCRIPT) {
            sea.attemptToTravel(direction, timestamp);
            sea.attemptToAttack(Weapon.CUTLASS, timestamp);
            sea.recalculateGameState(timestamp + 2_000L);
            timestamp += 1L;
        }
        return sea;
    }

    private static String snapshot(Sea sea) {
        PlayerDetails player = sea.playerDetails();
        StringBuilder builder = new StringBuilder();
        builder.append("pos=").append(player.position().q()).append(',').append(player.position().r());
        builder.append(";health=").append(player.health());
        builder.append(";food=").append(player.food());
        builder.append(";treasures=").append(player.capturedTreasures());
        builder.append(";ammo=").append(player.ammunitionByWeapon());
        builder.append(";gameOver=").append(sea.isGameOver());
        builder.append(";log=").append(sea.recentLog());
        return builder.toString();
    }

    private static String worldSnapshot(Sea sea) {
        StringBuilder builder = new StringBuilder();
        for (Hex hex : sea.generatedHexes()) {
            builder.append(hex.q()).append(',').append(hex.r())
                .append(':').append(sea.whatsAt(hex).pendingEvent().name()).append(';');
        }
        return builder.toString();
    }

    private static String seaStateSnapshot(Sea sea) {
        PlayerDetails player = sea.playerDetails();
        return playerLine(player.position(), player.health(), player.food())
            + ";treasures=" + player.capturedTreasures()
            + ";ammo=" + player.ammunitionByWeapon()
            + ";log=" + sea.recentLog();
    }

    private static String stateSnapshot(GameState state) {
        Player player = state.player();
        return playerLine(player.position(), player.health(), player.food())
            + ";treasures=" + player.capturedTreasures()
            + ";ammo=" + player.ammunitionByWeapon()
            + ";log=" + state.recentLog();
    }

    private static String playerLine(Hex position, int health, int food) {
        return "pos=" + position.q() + "," + position.r() + ";health=" + health + ";food=" + food;
    }
}
