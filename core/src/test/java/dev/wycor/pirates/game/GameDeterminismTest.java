package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameDeterminismTest {

    private static final Direction[] SCRIPT = {
        Direction.EAST, Direction.EAST, Direction.SOUTHEAST, Direction.WEST,
        Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.EAST
    };

    @Test
    void sameSeedAndInputsProduceIdenticalObservableState() {
        Game first = playScript(123456789L);
        Game second = playScript(123456789L);

        assertThat(snapshot(second)).isEqualTo(snapshot(first));
    }

    @Test
    void differentSeedsDivergeForTheSameInputs() {
        Game first = playScript(1L);
        Game second = playScript(2L);

        assertThat(worldSnapshot(second)).isNotEqualTo(worldSnapshot(first));
    }

    private static Game playScript(long seed) {
        GameRandom gameRandom = new GameRandom(seed);
        TileFactory tileFactory = new TileFactory(gameRandom.world(), gameRandom.monster());
        AttackResolver attackResolver = new AttackResolver(gameRandom.monster());
        HazardEngine hazardEngine = new HazardEngine(gameRandom.hazard());
        ItemEngine itemEngine = new ItemEngine();
        Game game = new Game(tileFactory, attackResolver, hazardEngine, itemEngine);
        game.startNewGame(0L);

        long timestamp = 1L;
        for (Direction direction : SCRIPT) {
            game.attemptToTravel(direction, timestamp);
            game.attemptToAttack(Weapon.CUTLASS, timestamp);
            game.recalculateGameState(timestamp + 2_000L);
            timestamp += 1L;
        }
        return game;
    }

    private static String snapshot(Game game) {
        PlayerDetails player = game.playerDetails();
        return "pos=" + player.position().q() + ',' + player.position().r() +
            ";health=" + player.health() +
            ";food=" + player.food() +
            ";treasures=" + player.capturedTreasures() +
            ";ammo=" + player.ammunitionByWeapon() +
            ";gameOver=" + game.isGameOver() +
            ";log=" + game.recentLog();
    }

    private static String worldSnapshot(Game game) {
        StringBuilder builder = new StringBuilder();
        for (Hex hex : game.generatedHexes()) {
            builder.append(hex.q()).append(',').append(hex.r())
                .append(':').append(game.whatsAt(hex).pendingEvent().name()).append(';');
        }
        return builder.toString();
    }

    private static String playerLine(Hex position, int health, int food) {
        return "pos=" + position.q() + "," + position.r() + ";health=" + health + ";food=" + food;
    }
}
