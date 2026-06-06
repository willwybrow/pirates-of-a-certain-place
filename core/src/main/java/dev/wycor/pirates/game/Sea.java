package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.*;
import java.util.stream.Stream;

public class Sea {
    private static final int MAX_LOG_LINES = 60;

    private PlayerDetails playerDetails;
    private final TileFactory tileFactory;

    private Hex headingTo; // null when still
    private boolean attackRequested;
    private boolean fleeRequested;

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(500);
    private final ArrayDeque<String> log = new ArrayDeque<>();

    public Sea() {
        this(new TileFactory());
    }

    public Sea(TileFactory tileFactory) {
        this.tileFactory = Objects.requireNonNull(tileFactory, "tileFactory");
        startNewGame();
    }

    public Hex playerPosition() {
        return this.playerDetails.position();
    }

    public int playerHealth() {
        return this.playerDetails.health();
    }

    public int playerMaxHealth() {
        return this.playerDetails.maxHealth();
    }

    public int playerFood() {
        return this.playerDetails.food();
    }

    public boolean isGameOver() {
        return this.playerDetails.isDead();
    }

    public Optional<Hex> getPlayerDestination() {
        return Optional.ofNullable(headingTo).filter(playerPosition().neighbours()::contains);
    }

    public boolean hasActiveCombat() {
        return getPlayerDestination()
            .map(destination -> {
                Combat combat = whatsAt(destination).getCombatEvent(playerDetails);
                return combat != null && combat.inProgress();
            })
            .orElse(false);
    }

    public Sea getGameState() {
        /*
        1. the player has already input the command to go to a tile (by clicking the direction button to sail there), setting headingTo
        2. the tile is generated (if it's not generated already)
        3. the tile is revealed (if it's not visible already)
        4. while there's a living enemy on the tile, and the intention is still to move there (fleeing is possible which cancels the movement) combat ensues
        5. once combat is null or over, rewards are granted
        6. the player's position is set to the new tile and the intended movement is wiped
         */

        if (isGameOver()) {
            clearActionRequests();
            return this;
        }

        Optional<Hex> destination = getPlayerDestination();
        if (destination.isEmpty()) {
            clearActionRequests();
            return this;
        }

        Hex destinationHex = destination.get();
        SeaTile destinationTile = whatsAt(destinationHex).spy(); // 2. and 3. -- generate and reveal

        Combat combatInProgress = destinationTile.getCombatEvent(playerDetails);

        if (combatInProgress != null && combatInProgress.inProgress()) {
            if (fleeRequested) {
                destinationTile.onPlayerFled();
                headingTo = null;
                addLog("You broke off and stayed at " + playerPosition() + ".");
                clearActionRequests();
                return this;
            }

            if (!attackRequested) {
                clearActionRequests();
                return this;
            }

            combatInProgress.resolveRound().forEach(this::addAttackLog);

            if (isGameOver()) {
                headingTo = null;
                addGameOverLog();
                clearActionRequests();
                return this;
            }

            if (combatInProgress.inProgress()) {
                clearActionRequests();
                return this;
            }

            addLog("Combat ended at " + destinationHex + ".");
        }

        if (!destinationTile.isPlayerRewarded()) {
            destinationTile.applyRewards(playerDetails);
            addLog("Claimed rewards at " + destinationHex + ".");
        }

        playerDetails.moveTo(destinationHex);
        playerDetails.consumeTravelSupplies();
        headingTo = null;
        addLog("Arrived at " + destinationHex + ".");

        if (isGameOver()) {
            addGameOverLog();
        }

        clearActionRequests();
        return this;
    }

    public SeaTile whatsAt(Hex location) {
        return generatedHexagons.computeIfAbsent(location, tileFactory::create);
    }

    public void attemptToTravel(Direction direction) {
        if (isGameOver()) {
            return;
        }

        if (getPlayerDestination().isEmpty()) {
            Hex headingFrom = playerDetails.position();
            this.headingTo = direction.move(headingFrom);

            SeaTile upcomingThing = whatsAt(headingTo).spy();
            addLog("Set course " + direction + " and spied " + upcomingThing.pendingEvent().name() + ".");
        }

        getGameState();
    }

    public void attemptToAttack() {
        if (isGameOver()) {
            return;
        }

        this.attackRequested = true;
        getGameState();
    }

    public void attemptToFlee() {
        if (isGameOver()) {
            return;
        }

        this.fleeRequested = true;
        getGameState();
    }

    public void startNewGame() {
        this.generatedHexagons.clear();
        this.log.clear();
        this.headingTo = null;
        clearActionRequests();

        this.playerDetails = new PlayerDetails(Hex.ORIGIN);
        this.generatedHexagons.put(Hex.ORIGIN, SeaTile.startingSquare());
        revealAround(Hex.ORIGIN);
        addLog("Set sail from home waters.");
    }

    public Set<Hex> explored() {
        return generatedHexagons.keySet();
    }

    public Stream<Hex> walkTheSpiral(int layers) {
        Hex start = getPlayerDestination().orElseGet(this::playerPosition);
        HashSet<Hex> hexes = new HashSet<>();
        var n = Math.abs(layers);

        for (int q = -n; q<= n; q++) {
            for (int r = Math.max(-n, -q-n); r <= Math.min(n, -q+n); r++) {
                hexes.add(new Hex(start.q() + q, start.r() + r));
            }
        }

        return hexes.stream();
    }

    public List<String> recentLog() {
        return new ArrayList<>(this.log);
    }

    private void revealAround(Hex position) {
        whatsAt(position).spy();
        position.neighbours().forEach(neighbour -> whatsAt(neighbour).spy());
    }

    private void addAttackLog(Attack attack) {
        addLog(attack.initiator().name() + " hit " + attack.defender().name() + " for " + attack.actualDamage() + ".");
    }

    private void clearActionRequests() {
        this.attackRequested = false;
        this.fleeRequested = false;
    }

    private void addLog(String line) {
        log.addFirst(line);
        while (log.size() > MAX_LOG_LINES) {
            log.removeLast();
        }
    }

    private void addGameOverLog() {
        if (log.isEmpty() || !"GAME OVER.".equals(log.peekFirst())) {
            addLog("GAME OVER.");
        }
    }
}
