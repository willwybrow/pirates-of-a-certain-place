package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.ItemTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Single-game state and simulation logic.
 *
 * <p>This class intentionally owns both gameplay state and rule execution so callers do not need to
 * hop through intermediate engine/state wrappers to get from {@code Game} to {@code World} data.
 */
public class Game {
    private static final int MAX_LOG_LINES = 60;
    private static final long TILE_TRAVEL_DELAY_MILLIS = 1_000L;
    private static final long TILE_HIGHLIGHT_DURATION_MILLIS = 1_500L;

    private final ArrayDeque<String> logLines = new ArrayDeque<>();
    private final TileFactory tileFactory;
    private final AttackResolver attackResolver;
    private final HazardEngine hazardEngine;
    private final ItemEngine itemEngine;

    private final ArrayList<TimedTileHighlight> timedTileHighlights = new ArrayList<>();

    private GameLog log;
    private World world;
    private Player player;
    private ActiveTravel activeTravel;

    public Game(TileFactory tileFactory,
                AttackResolver attackResolver,
                HazardEngine hazardEngine,
                ItemEngine itemEngine) {
        this.tileFactory = tileFactory;
        this.attackResolver = attackResolver;
        this.hazardEngine = hazardEngine;
        this.itemEngine = itemEngine;
        startNewGame(0L);
    }

    public Game startNewGame(long timestamp) {
        this.log = new GameLog();
        this.world = tileFactory.generate();
        this.player = new Player(Hex.ORIGIN);
        this.activeTravel = null;
        this.timedTileHighlights.clear();
        addLog("Set sail from home waters.");
        advance(timestamp);
        return this;
    }

    public Game recalculateGameState(long timestampMillis) {
        advance(timestampMillis);
        return this;
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

    public PlayerDetails playerDetails() {
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

    public boolean isGameOver() {
        return gameEndStatus() != GameEndStatus.ONGOING;
    }

    public String gameOverMessage() {
        return gameOverMessage(gameEndStatus());
    }

    public Optional<Hex> getPlayerDestination() {
        return outstandingTravel()
            .map(ActiveTravel::destination)
            .filter(World::isWithinWorld);
    }

    public boolean hasActiveCombat() {
        return liveOpponentAtDestination().isPresent();
    }

    public Optional<CombatantDetails> currentOpponentDetails() {
        return liveOpponentAtDestination()
            .map(opponent -> new CombatantDetails(opponent.name(), opponent.health(), opponent.maxHealth()));
    }

    public SeaTile whatsAt(Hex location) {
        return world.whatsAt(location);
    }

    public List<Hex> generatedHexes() {
        return world.generatedHexes();
    }

    public List<String> recentLog() {
        return new ArrayList<>(this.logLines);
    }

    public boolean isAwaitingSpyglassDirection() {
        Optional<ActiveTravel> outstandingTravel = outstandingTravel();
        if (outstandingTravel.isEmpty()) {
            return false;
        }

        ActiveTravel travel = outstandingTravel.get();
        SeaTile destinationTile = world.whatsAt(travel.destination());
        return isUnresolvedSpyglassTile(destinationTile) && !travel.hasSpyglassRevealDirection();
    }

    public Optional<Hex> transientFocusHex(long timestampMillis) {
        TimedTileHighlight latestGreenHighlight = null;
        for (TimedTileHighlight timedTileHighlight : this.timedTileHighlights) {
            if (timestampMillis >= timedTileHighlight.expiresAtMillis()) {
                continue;
            }

            TileHighlight highlight = timedTileHighlight.highlight();
            if (highlight.color() != TileHighlight.Color.GREEN) {
                continue;
            }

            if (latestGreenHighlight == null
                || timedTileHighlight.expiresAtMillis() > latestGreenHighlight.expiresAtMillis()) {
                latestGreenHighlight = timedTileHighlight;
            }
        }

        return latestGreenHighlight == null
            ? Optional.empty()
            : Optional.of(latestGreenHighlight.highlight().hex());
    }

    public List<TileHighlight> activeTileHighlights() {
        ArrayList<TileHighlight> highlights = new ArrayList<>(this.timedTileHighlights.size());
        for (TimedTileHighlight timedTileHighlight : this.timedTileHighlights) {
            highlights.add(timedTileHighlight.highlight());
        }
        return highlights;
    }

    private void submit(GameInput input) {
        log.append(input);
        applyInput(input);
    }

    private void applyInput(GameInput input) {
        processGameplayInput(input);
        advance(input.timestampMillis());
    }

    private void advance(long timestampMillis) {
        expireTransientVisuals(timestampMillis);
        resolveActiveTravel(timestampMillis);
        if (isGameOver()) {
            addGameOverLog();
        }
    }

    private void processGameplayInput(GameInput input) {
        if (isGameOver()) {
            return;
        }

        if (input instanceof GameInput.Travel) {
            processTravelInput((GameInput.Travel) input);
            return;
        }

        Optional<Monster> liveOpponent = liveOpponentAtDestination();
        if (liveOpponent.isEmpty()) {
            return;
        }
        Monster opponent = liveOpponent.get();

        if (input instanceof GameInput.Attack) {
            processAttackInput((GameInput.Attack) input, opponent);
            return;
        }

        if (input instanceof GameInput.Flee) {
            processFleeInput();
        }
    }

    private void processTravelInput(GameInput.Travel travel) {
        Optional<ActiveTravel> outstandingTravel = outstandingTravel();
        if (outstandingTravel.isPresent()) {
            if (consumeSpyglassDirectionInput(outstandingTravel.get(), travel)) {
                return;
            }
            return;
        }

        Hex destination = travel.direction().move(player.position());
        beginTravel(destination, travel.timestampMillis());
    }

    private boolean consumeSpyglassDirectionInput(ActiveTravel travel, GameInput.Travel input) {
        SeaTile destinationTile = world.whatsAt(travel.destination());
        if (!isUnresolvedSpyglassTile(destinationTile) || travel.hasSpyglassRevealDirection()) {
            return false;
        }

        travel.setSpyglassRevealDirection(input.direction());

        ItemEngine.Resolution resolution = itemEngine.resolveSpyglassDirection(
            player.position(),
            input.direction(),
            world
        );
        applyItemResolution(resolution, input.timestampMillis());
        return true;
    }

    private void processAttackInput(GameInput.Attack attack, Monster opponent) {
        Weapon weapon = attack.weapon();
        if (!player.canWield(weapon)) {
            return;
        }
        if (!player.consumeAmmunition(weapon)) {
            addLog("Out of ammunition for " + weapon.displayName() + ".");
            return;
        }

        this.attackResolver.resolveCombatRound(player, opponent, weapon).forEach(this::addAttackLog);
    }

    private void processFleeInput() {
        Optional<ActiveTravel> outstandingTravel = outstandingTravel();
        if (outstandingTravel.isEmpty()) {
            return;
        }

        SeaTile destinationTile = world.whatsAt(outstandingTravel.get().destination());
        destinationTile.onPlayerFled();
        cancelActiveTravel();
        addLog("You broke off and stayed at " + player.position() + ".");
    }

    private void resolveActiveTravel(long timestampMillis) {
        Optional<ActiveTravel> outstandingTravel = outstandingTravel();
        if (outstandingTravel.isEmpty()) {
            return;
        }

        ActiveTravel travel = outstandingTravel.get();
        Hex destinationHex = travel.destination();
        if (!World.isWithinWorld(destinationHex)) {
            cancelActiveTravel();
            return;
        }

        SeaTile destinationTile = world.whatsAt(destinationHex);
        boolean wasSpied = destinationTile.isSpied();
        destinationTile.spy();
        if (!wasSpied) {
            addLog("Set course " + directionTo(player.position(), destinationHex)
                + " and spied " + destinationTile.pendingEvent().name() + ".");
        }

        Monster opponent = destinationTile.getCombatant();
        if (opponent != null && !opponent.isDead()) {
            return;
        }

        if (!destinationTile.isCompleted() && (destinationTile.getItem() != null || destinationTile.getHazard() != null || (destinationTile.getReward() != null && destinationTile.getReward().treasure() != null))) {
            long readyAtMillis = travel.timestampMillis() + TILE_TRAVEL_DELAY_MILLIS;
            if (timestampMillis < readyAtMillis) {
                return;
            }
        }

        Optional.ofNullable(this.hazardEngine.resolve(destinationTile, player, world)).ifPresent(this::addLog);

        if (isUnresolvedSpyglassTile(destinationTile)) {
            if (!travel.spyglassPromptShown()) {
                ItemEngine.Resolution resolution = itemEngine.resolveEncounter(Item.SPYGLASS, destinationHex, world);
                applyItemResolution(resolution, timestampMillis);
                travel.markSpyglassPromptShown();
            }

            if (!travel.hasSpyglassRevealDirection()) {
                return;
            }
        }

        if (isUnresolvedMapTile(destinationTile) && !travel.mapEffectApplied()) {
            ItemEngine.Resolution resolution = itemEngine.resolveEncounter(Item.MAP, destinationHex, world);
            applyItemResolution(resolution, timestampMillis);
            travel.markMapEffectApplied();
        }

        if (isMovementBlockedByMapPan(destinationTile, timestampMillis)) {
            return;
        }

        if (!destinationTile.isCompleted()) {
            applyReward(destinationTile.applyRewards());
            if (destinationTile.getItem() == Item.SEXTANT) {
                ItemEngine.Resolution resolution = itemEngine.resolveEncounter(Item.SEXTANT, destinationHex, world);
                applyItemResolution(resolution, timestampMillis);
            }
        }

        player.moveTo(destinationHex);
        player.consumeTravelSupplies();
        clearActiveTravel();
    }

    private void applyReward(Reward reward) {
        if (reward == null) {
            return;
        }
        if (reward.health() != 0) {
            player.heal(reward.health());
        }
        if (reward.food() != 0) {
            player.restock(reward.food());
        }
        reward.ammunitionByWeapon().forEach(player::restockAmmunition);
        if (reward.treasure() != null) {
            player.captureTreasure(reward.treasure());
            addLog("Recovered " + reward.treasure().displayName() + ".");
        }
    }

    private void addAttackLog(Attack attack) {
        addLog(attack.initiator().name() + " hit " + attack.defender().name()
            + " for " + attack.actualDamage() + ".");
    }

    private void addGameOverLog() {
        String message = gameOverMessage(gameEndStatus());
        if (!message.isEmpty() && (logLines.isEmpty() || !message.equals(logLines.peekFirst()))) {
            addLog(message);
        }
    }

    private GameEndStatus gameEndStatus() {
        if (player == null) {
            return GameEndStatus.PRESTARTED;
        }
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

    static String gameOverMessage(GameEndStatus status) {
        switch (status) {
            case PRESTARTED:
                return "Welcome to Pirate's Plunder!";
            case DEFEATED:
                return "You have been defeated!";
            case STARVED:
                return "Your crew has starved!";
            case TREASURES_FOUND:
                return "You found all the treasures!";
            default:
                return "";
        }
    }

    private void addLog(String line) {
        logLines.addFirst(line);
        while (logLines.size() > MAX_LOG_LINES) {
            logLines.removeLast();
        }
    }

    private void applyItemResolution(ItemEngine.Resolution resolution, long timestampMillis) {
        resolution.logLines().forEach(this::addLog);

        for (TileHighlight highlight : resolution.highlights()) {
            this.timedTileHighlights.add(new TimedTileHighlight(
                highlight,
                timestampMillis + TILE_HIGHLIGHT_DURATION_MILLIS
            ));
        }
    }

    private void expireTransientVisuals(long timestampMillis) {
        this.timedTileHighlights.removeIf(highlight -> timestampMillis >= highlight.expiresAtMillis());
    }

    private void beginTravel(Hex destination, long timestampMillis) {
        this.activeTravel = new ActiveTravel(destination, timestampMillis);
    }

    private void cancelActiveTravel() {
        this.activeTravel = null;
    }

    private void clearActiveTravel() {
        this.activeTravel = null;
    }

    private Optional<ActiveTravel> outstandingTravel() {
        if (isGameOver() || activeTravel == null) {
            return Optional.empty();
        }

        Hex destination = activeTravel.destination;
        if (destination == null || player.position().equals(destination)) {
            return Optional.empty();
        }

        return Optional.of(activeTravel);
    }

    private Optional<Monster> liveOpponentAtDestination() {
        return getPlayerDestination()
            .map(world::whatsAt)
            .map(SeaTile::getCombatant)
            .filter(opponent -> opponent != null && !opponent.isDead());
    }

    private static Direction directionTo(Hex from, Hex destination) {
        for (Direction direction : Direction.values()) {
            if (direction.move(from).equals(destination)) {
                return direction;
            }
        }
        return null;
    }

    private static boolean isUnresolvedMapTile(SeaTile tile) {
        return tile.getItem() == Item.MAP && !tile.isCompleted();
    }

    private boolean isMovementBlockedByMapPan(SeaTile tile, long timestampMillis) {
        return tile.getItem() == Item.MAP && transientFocusHex(timestampMillis).isPresent();
    }

    private static boolean isUnresolvedSpyglassTile(SeaTile tile) {
        return tile.getItem() == Item.SPYGLASS && !tile.isCompleted();
    }

    private enum GameEndStatus {
        PRESTARTED,
        ONGOING,
        DEFEATED,
        STARVED,
        TREASURES_FOUND
    }

    private static final class ActiveTravel {
        private final Hex destination;
        private final long timestampMillis;
        private Direction spyglassRevealDirection;
        private boolean spyglassPromptShown;
        private boolean mapEffectApplied;

        private ActiveTravel(Hex destination, long timestampMillis) {
            this.destination = destination;
            this.timestampMillis = timestampMillis;
        }

        private Hex destination() {
            return this.destination;
        }

        private long timestampMillis() {
            return this.timestampMillis;
        }

        private boolean hasSpyglassRevealDirection() {
            return this.spyglassRevealDirection != null;
        }

        private void setSpyglassRevealDirection(Direction direction) {
            this.spyglassRevealDirection = direction;
        }

        private boolean spyglassPromptShown() {
            return this.spyglassPromptShown;
        }

        private void markSpyglassPromptShown() {
            this.spyglassPromptShown = true;
        }

        private boolean mapEffectApplied() {
            return this.mapEffectApplied;
        }

        private void markMapEffectApplied() {
            this.mapEffectApplied = true;
        }
    }

    private static final class TimedTileHighlight {
        private final TileHighlight highlight;
        private final long expiresAtMillis;

        private TimedTileHighlight(TileHighlight highlight, long expiresAtMillis) {
            this.highlight = highlight;
            this.expiresAtMillis = expiresAtMillis;
        }

        private TileHighlight highlight() {
            return this.highlight;
        }

        private long expiresAtMillis() {
            return this.expiresAtMillis;
        }
    }
}
