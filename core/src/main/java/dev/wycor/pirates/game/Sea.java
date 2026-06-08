package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * Facade over a single game. It wires together three separated concerns:
 *
 * <ul>
 *   <li>the {@link GameLog} &mdash; the authoritative seed + ordered list of player inputs;</li>
 *   <li>the {@link GameEngine} &mdash; pure simulation rules that fold inputs into state;</li>
 *   <li>the cached {@link GameState} &mdash; an in-memory "computed up to here" snapshot.</li>
 * </ul>
 *
 * <p>Live play advances the cached state incrementally as inputs arrive (fast). Because the log
 * plus the seed fully determine the game, the exact same state can be rebuilt from scratch at any
 * time via {@link GameEngine#replay}. {@code Sea} exposes the read-only {@link GameView} the UIs
 * render from.
 */
public class Sea implements GameView {

    private final TileFactory tileFactory;
    private final GameEngine engine;
    private final Random seedSource = new Random();

    private GameLog log;
    private GameState state;

    public Sea(TileFactory tileFactory) {
        this.tileFactory = Objects.requireNonNull(tileFactory, "tileFactory");
        this.engine = new GameEngine(tileFactory);
        startNewGame(seedSource.nextLong(), 0L);
    }

    /** Starts a fresh game with a randomly chosen seed. */
    public void startNewGame(long timestampMillis) {
        startNewGame(seedSource.nextLong(), timestampMillis);
    }

    /** Starts a fresh game with an explicit seed, making the game deterministically reproducible. */
    public void startNewGame(long seed, long timestampMillis) {
        this.log = new GameLog(seed);
        this.state = engine.newGame(seed);
        engine.advance(state, timestampMillis);
    }

    public long seed() {
        return this.log.seed();
    }

    public List<GameInput> inputs() {
        return this.log.inputs();
    }

    /**
     * Advances the cached state's time-gated consequences (such as treasure pickup delays) up to the
     * given timestamp. Live play uses this incremental cache; the equivalent from-scratch rebuild is
     * available via {@link #replay(long)}.
     */
    public Sea recalculateGameState(long timestampMillis) {
        engine.advance(state, timestampMillis);
        return this;
    }

    /**
     * Rebuilds the game state from scratch by replaying the recorded log against the seed, up to the
     * given timestamp. Equivalent to the incrementally-maintained cache, and used to verify
     * determinism or to load a game from its log.
     */
    public GameState replay(long upToTimestampMillis) {
        return engine.replay(log.seed(), log.inputs(), upToTimestampMillis);
    }

    public void attemptToTravel(Direction direction, long timestampMillis) {
        submit(GameInput.travel(direction, timestampMillis));
    }

    public void attemptToAttack(long timestampMillis) {
        attemptToAttack(Weapon.CUTLASS, timestampMillis);
    }

    public void attemptToAttack(Weapon weapon, long timestampMillis) {
        submit(GameInput.attack(weapon, timestampMillis));
    }

    public void attemptToFlee(long timestampMillis) {
        submit(GameInput.flee(timestampMillis));
    }

    private void submit(GameInput input) {
        log.append(input);
        engine.applyInput(state, input);
    }

    // --- GameView ---

    @Override
    public PlayerDetails playerDetails() {
        Player player = state.player();
        return new PlayerDetails(
            player.position(),
            player.health(),
            player.maxHealth(),
            player.food(),
            player.capturedTreasures(),
            player.ammunitionByWeapon(),
            player.wieldableWeapons()
        );
    }

    @Override
    public boolean isGameOver() {
        return state.isGameOver();
    }

    @Override
    public String gameOverMessage() {
        return GameEngine.gameOverMessage(state.gameEndStatus());
    }

    @Override
    public Optional<Hex> getPlayerDestination() {
        return state.playerDestination();
    }

    @Override
    public boolean hasActiveCombat() {
        return state.hasActiveCombat();
    }

    @Override
    public Optional<CombatantDetails> currentOpponentDetails() {
        return state.liveOpponentAtDestination()
            .map(opponent -> new CombatantDetails(opponent.name(), opponent.health(), opponent.maxHealth()));
    }

    @Override
    public SeaTile whatsAt(Hex location) {
        return state.world().whatsAt(location);
    }

    @Override
    public List<Hex> generatedHexes() {
        return state.world().generatedHexes();
    }

    @Override
    public List<String> recentLog() {
        return state.recentLog();
    }
}
