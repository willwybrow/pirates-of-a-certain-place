package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Single-game state and simulation logic.
 *
 * <p>This class intentionally owns both gameplay state and rule execution so callers do not need to
 * hop through intermediate engine/state wrappers to get from {@code Game} to {@code World} data.
 */
public class Game {
    private static final int MAX_LOG_LINES = 60;
    private static final long TILE_TRAVEL_DELAY_MILLIS = 1_000L;
    private static final int ICEBERG_DAMAGE = 20;

    private final ArrayDeque<String> logLines = new ArrayDeque<>();
    private final GameRandom gameRandom;
    private final TileFactory tileFactory;

    private GameLog log;
    private World world;
    private Player player;
    private ActiveTravel activeTravel;

    public Game(GameRandom gameRandom, TileFactory tileFactory) {
        this.gameRandom = gameRandom;
        this.tileFactory = tileFactory;
        startNewGame(System.currentTimeMillis());
    }

    public Game startNewGame(long timestamp) {
        this.log = new GameLog();
        this.world = tileFactory.generate();
        this.player = new Player(Hex.ORIGIN);
        this.activeTravel = null;
        addLog("Set sail from home waters.");
        advance(timestamp);
        return this;
    }

    public List<GameInput> inputs() {
        return this.log.inputs();
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

    private void submit(GameInput input) {
        log.append(input);
        applyInput(input);
    }

    private void applyInput(GameInput input) {
        processGameplayInput(input);
        advance(input.timestampMillis());
    }

    private void advance(long timestampMillis) {
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
        if (outstandingTravel().isPresent()) {
            return;
        }

        Hex destination = travel.direction().move(player.position());
        beginTravel(destination, travel.timestampMillis());
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

        resolveCombatRound(opponent, weapon).forEach(this::addAttackLog);
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

    private List<Attack> resolveCombatRound(Monster opponent, Weapon weapon) {
        ArrayList<Attack> attacksThisRound = new ArrayList<>(2);

        Attack playerAttack = new Attack(player, opponent, weapon);
        attacksThisRound.add(opponent.receiveAttack(playerAttack, gameRandom.combat()));

        if (!opponent.isDead()) {
            attacksThisRound.add(opponent.strike(player, gameRandom.combat()));
        }

        return attacksThisRound;
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

        boolean isPendingHazard = destinationTile instanceof HazardTile && !destinationTile.isCompleted();
        boolean isPendingTreasure = destinationTile instanceof TreasureTile && !destinationTile.isPlayerRewarded();
        if (isPendingHazard || isPendingTreasure) {
            long readyAtMillis = travel.timestampMillis() + TILE_TRAVEL_DELAY_MILLIS;
            if (timestampMillis < readyAtMillis) {
                return;
            }
        }

        if (isPendingHazard) {
            resolveHazard((HazardTile) destinationTile);
        }

        if (!destinationTile.isPlayerRewarded()) {
            applyReward(destinationTile.applyRewards());
        }

        player.moveTo(destinationHex);
        player.consumeTravelSupplies();
        clearActiveTravel();
    }

    private void resolveHazard(HazardTile hazardTile) {
        switch (hazardTile.hazard()) {
            case ICEBERG:
                resolveIceberg();
                break;
            case WHIRLPOOL:
                resolveWhirlpool();
                break;
            default:
                break;
        }
        hazardTile.trigger();
    }

    private void resolveIceberg() {
        player.takeDamage(ICEBERG_DAMAGE);
        addLog("An iceberg gouged the hull for " + ICEBERG_DAMAGE + " damage.");
    }

    private void resolveWhirlpool() {
        List<Treasure> capturedTreasures = player.capturedTreasureList();
        if (capturedTreasures.isEmpty()) {
            addLog("A whirlpool churned past, but you had no treasure to lose.");
            return;
        }

        Treasure lostTreasure = capturedTreasures.get(gameRandom.hazard().nextInt(capturedTreasures.size()));
        boolean rehidden = world.rehideTreasureUnderUnexploredTile(lostTreasure, gameRandom.hazard());
        if (!rehidden) {
            addLog("A whirlpool churned past, but there was nowhere left to hide your treasure.");
            return;
        }

        player.loseTreasure(lostTreasure);
        addLog("A whirlpool swallowed the " + lostTreasure.displayName() + " and hid it anew.");
    }

    private void applyReward(Reward reward) {
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

    private void beginTravel(Hex destination, long timestampMillis) {
        this.activeTravel = new ActiveTravel(destination, timestampMillis);
    }

    private void cancelActiveTravel() {
        if (activeTravel != null) {
            activeTravel.cancelled = true;
        }
    }

    private void clearActiveTravel() {
        this.activeTravel = null;
    }

    private Optional<ActiveTravel> outstandingTravel() {
        if (isGameOver() || activeTravel == null || activeTravel.cancelled) {
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

    private enum GameEndStatus {
        ONGOING,
        DEFEATED,
        STARVED,
        TREASURES_FOUND
    }

    private static final class ActiveTravel {
        private final Hex destination;
        private final long timestampMillis;
        private boolean cancelled;

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
    }
}
