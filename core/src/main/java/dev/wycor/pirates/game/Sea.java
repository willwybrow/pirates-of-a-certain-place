package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.*;
import java.util.stream.Stream;

public class Sea {
    private static final int MAX_LOG_LINES = 60;

    private Player player;
    private final TileFactory tileFactory;

    private Hex headingTo; // null when still
    private boolean attackRequested;
    private boolean fleeRequested;
    private Weapon requestedWeapon;

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(500);
    private final ArrayDeque<String> log = new ArrayDeque<>();

    public Sea() {
        this(new TileFactory());
    }

    public Sea(TileFactory tileFactory) {
        this.tileFactory = Objects.requireNonNull(tileFactory, "tileFactory");
        startNewGame();
    }

    public PlayerDetails playerDetails() {
        return new PlayerDetails(
            player.position(),
            player.health(),
            player.maxHealth(),
            player.food(),
            player.capturedTreasures()
        );
    }

    public boolean isGameOver() {
        return this.player.isDead();
    }

    public Optional<Hex> getPlayerDestination() {
        return Optional.ofNullable(headingTo).filter(player.position().neighbours()::contains);
    }

    public boolean hasActiveCombat() {
        return getPlayerDestination()
            .map(destination -> whatsAt(destination).getCombatant())
            .map(opponent -> !opponent.isDead())
            .orElse(false);
    }

    public Sea recalculateGameState() {
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

        Combatant opponent = destinationTile.getCombatant();

        if (opponent != null && !opponent.isDead()) {
            if (fleeRequested) {
                destinationTile.onPlayerFled();
                headingTo = null;
                addLog("You broke off and stayed at " + player.position() + ".");
                clearActionRequests();
                return this;
            }

            if (!attackRequested) {
                clearActionRequests();
                return this;
            }

            resolveCombatRound(opponent, requestedWeapon).forEach(this::addAttackLog);

            if (isGameOver()) {
                headingTo = null;
                addGameOverLog();
                clearActionRequests();
                return this;
            }

            if (!opponent.isDead()) {
                clearActionRequests();
                return this;
            }
        }

        if (!destinationTile.isPlayerRewarded()) {
            Reward reward = destinationTile.applyRewards();
            applyReward(reward);
        }

        player.moveTo(destinationHex);
        player.consumeTravelSupplies();
        headingTo = null;

        if (isGameOver()) {
            addGameOverLog();
        }

        clearActionRequests();
        return this;
    }

    public SeaTile whatsAt(Hex location) {
        return generatedHexagons.computeIfAbsent(location, hex -> tileFactory.create(hex, player.capturedTreasures()));
    }

    public void attemptToTravel(Direction direction) {
        if (isGameOver()) {
            return;
        }

        if (getPlayerDestination().isEmpty()) {
            Hex headingFrom = player.position();
            this.headingTo = direction.move(headingFrom);

            SeaTile upcomingThing = whatsAt(headingTo).spy();
            addLog("Set course " + direction + " and spied " + upcomingThing.pendingEvent().name() + ".");
        }

        recalculateGameState();
    }

    public void attemptToAttack() {
        attemptToAttack(Weapon.CUTLASS);
    }

    public void attemptToAttack(Weapon weapon) {
        if (isGameOver()) {
            return;
        }

        this.requestedWeapon = Objects.requireNonNull(weapon, "weapon");
        this.attackRequested = true;
        recalculateGameState();
    }

    public void attemptToFlee() {
        if (isGameOver()) {
            return;
        }

        this.fleeRequested = true;
        recalculateGameState();
    }

    public void startNewGame() {
        this.generatedHexagons.clear();
        this.log.clear();
        this.headingTo = null;
        clearActionRequests();

        this.player = new Player(Hex.ORIGIN);
        this.generatedHexagons.put(Hex.ORIGIN, SeaTile.startingSquare());
        addLog("Set sail from home waters.");
    }

    public Stream<Hex> walkTheSpiral(int layers) {
        Hex start = getPlayerDestination().orElseGet(player::position);
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

    private void addAttackLog(Attack attack) {
        addLog(attack.initiator().name() + " hit " + attack.defender().name() + " for " + attack.actualDamage() + ".");
    }

    private List<Attack> resolveCombatRound(Combatant opponent, Weapon weapon) {
        ArrayList<Attack> attacksThisRound = new ArrayList<>(2);

        Attack playerAttack = new Attack(player, opponent, weapon, player.attack, opponent.defence);
        opponent.receiveAttack(playerAttack);
        attacksThisRound.add(playerAttack);

        if (!opponent.isDead()) {
            Attack opponentAttack = new Attack(opponent, player, null, opponent.attack, player.defence);
            player.receiveAttack(opponentAttack);
            attacksThisRound.add(opponentAttack);
        }

        return List.copyOf(attacksThisRound);
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
            addLog("Recovered " + reward.treasure().name() + ".");
        }
    }

    private void clearActionRequests() {
        this.attackRequested = false;
        this.fleeRequested = false;
        this.requestedWeapon = null;
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
