package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The computed, in-memory state of a game at a particular point in its input timeline.
 *
 * <p>This is purely a cache of "where we have computed up to": it is rebuildable at any time by
 * replaying the {@link GameLog} against the game seed. {@link GameEngine} owns all the rules that
 * mutate it; queries here are read-only views suitable for rendering and input gating.
 */
final class GameState {
    private static final int MAX_LOG_LINES = 60;

    private final GameRandom random;
    private final World world;
    private final Player player;
    private final ArrayDeque<String> log = new ArrayDeque<>();

    private ActiveTravel activeTravel;

    GameState(long seed, TileFactory tileFactory) {
        this.random = new GameRandom(seed);
        this.world = new World(tileFactory, random.worldGen());
        this.player = new Player(Hex.ORIGIN);
    }

    // --- accessors used by the engine ---

    Player player() {
        return this.player;
    }

    World world() {
        return this.world;
    }

    java.util.Random combatRandom() {
        return this.random.combat();
    }

    java.util.Random hazardRandom() {
        return this.random.hazard();
    }

    // --- active travel (replaces the old InputEvent travel flags) ---

    void beginTravel(Hex destination, long timestampMillis) {
        this.activeTravel = new ActiveTravel(destination, timestampMillis);
    }

    void cancelActiveTravel() {
        if (this.activeTravel != null) {
            this.activeTravel.cancelled = true;
        }
    }

    void clearActiveTravel() {
        this.activeTravel = null;
    }

    Optional<ActiveTravel> outstandingTravel() {
        if (isGameOver() || activeTravel == null || activeTravel.cancelled) {
            return Optional.empty();
        }

        Hex destination = activeTravel.destination;
        if (destination == null || player.position().equals(destination)) {
            return Optional.empty();
        }

        return Optional.of(activeTravel);
    }

    // --- queries shared by engine and view ---

    Optional<Hex> playerDestination() {
        return outstandingTravel()
            .map(travel -> travel.destination)
            .filter(World::isWithinWorld);
    }

    Optional<Monster> liveOpponentAtDestination() {
        return playerDestination()
            .map(world::whatsAt)
            .map(SeaTile::getCombatant)
            .filter(opponent -> opponent != null && !opponent.isDead());
    }

    boolean hasActiveCombat() {
        return liveOpponentAtDestination().isPresent();
    }

    GameEndStatus gameEndStatus() {
        if (player.hasCapturedAllTreasures()) {
            return GameEndStatus.TREASURES_FOUND;
        }
        if (player.isDead()) {
            return GameEndStatus.DEFEATED;
        }
        if (player.food() <= 0) {
            return GameEndStatus.STARVED;
        }
        return GameEndStatus.ONGOING;
    }

    boolean isGameOver() {
        return gameEndStatus() != GameEndStatus.ONGOING;
    }

    // --- log ---

    void addLog(String line) {
        log.addFirst(line);
        while (log.size() > MAX_LOG_LINES) {
            log.removeLast();
        }
    }

    String peekLatestLog() {
        return log.peekFirst();
    }

    boolean logIsEmpty() {
        return log.isEmpty();
    }

    List<String> recentLog() {
        return new ArrayList<>(this.log);
    }

    enum GameEndStatus {
        ONGOING,
        DEFEATED,
        STARVED,
        TREASURES_FOUND
    }

    /** Tracks the player's in-flight movement toward a destination tile. */
    static final class ActiveTravel {
        private final Hex destination;
        private final long timestampMillis;
        private boolean cancelled;

        private ActiveTravel(Hex destination, long timestampMillis) {
            this.destination = destination;
            this.timestampMillis = timestampMillis;
        }

        Hex destination() {
            return this.destination;
        }

        long timestampMillis() {
            return this.timestampMillis;
        }
    }
}
