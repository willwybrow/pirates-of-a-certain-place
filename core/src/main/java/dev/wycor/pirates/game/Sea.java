package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.*;
import java.util.stream.Stream;

public class Sea {
    private static final int MAX_LOG_LINES = 60;
    private static final long TREASURE_TILE_TRAVEL_DELAY_MILLIS = 1_000;

    private Player player;
    private final TileFactory tileFactory;

    private long nextInputSequence;

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(500);
    private final ArrayDeque<String> log = new ArrayDeque<>();
    private final ArrayList<InputEvent> inputEvents = new ArrayList<>();

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
        return nextUnprocessedTravelEvent().map(inputEvent -> inputEvent.resolveDestination(player.position()));
    }

    public boolean hasActiveCombat() {
        return getPlayerDestination()
            .map(destination -> whatsAt(destination).getCombatant())
            .map(opponent -> !opponent.isDead())
            .orElse(false);
    }

    public Optional<CombatantDetails> currentOpponentDetails() {
        return getPlayerDestination()
            .map(this::whatsAt)
            .map(SeaTile::getCombatant)
            .filter(opponent -> opponent != null && !opponent.isDead())
            .map(opponent -> new CombatantDetails(opponent.name(), opponent.health(), opponent.maxHealth()));
    }

    public Sea recalculateGameState() {
        return recalculateGameState(System.currentTimeMillis());
    }

    public Sea recalculateGameState(long timestampMillis) {
        /*
        1. the player has already input the command to go to a tile (by clicking the direction button to sail there), setting headingTo
        2. the tile is generated (if it's not generated already)
        3. the tile is revealed (if it's not visible already)
        4. while there's a living enemy on the tile, and the intention is still to move there (fleeing is possible which cancels the movement) combat ensues
        5. once combat is null or over, rewards are granted
        6. the player's position is set to the new tile and the intended movement is wiped
         */

        if (isGameOver()) {
            markAllUnprocessedInputsProcessed();
            pruneProcessedInputs();
            return this;
        }

        Optional<InputEvent> nextTravelInput = nextUnprocessedTravelEvent();
        if (nextTravelInput.isEmpty()) {
            markUnprocessedActionInputsProcessed();
            pruneProcessedInputs();
            return this;
        }

        InputEvent travelInput = nextTravelInput.get();
        markActionInputsBefore(travelInput.sequence);
        markTravelInputsAfter(travelInput.sequence);

        Hex destinationHex = travelInput.resolveDestination(player.position());
        SeaTile destinationTile = whatsAt(destinationHex).spy(); // 2. and 3. -- generate and reveal
        if (!travelInput.destinationRevealed) {
            addLog("Set course " + travelInput.direction + " and spied " + destinationTile.pendingEvent().name() + ".");
            travelInput.destinationRevealed = true;
        }

        Combatant opponent = destinationTile.getCombatant();

        if (opponent != null && !opponent.isDead()) {
            Optional<InputEvent> nextActionInput = nextUnprocessedActionInputAfter(travelInput.sequence);
            if (nextActionInput.isEmpty()) {
                pruneProcessedInputs();
                return this;
            }

            InputEvent actionInput = nextActionInput.get();
            actionInput.processed = true;

            if (actionInput.type == InputType.FLEE) {
                destinationTile.onPlayerFled();
                travelInput.processed = true;
                addLog("You broke off and stayed at " + player.position() + ".");
                pruneProcessedInputs();
                return this;
            }

            resolveCombatRound(opponent, actionInput.weapon).forEach(this::addAttackLog);

            if (isGameOver()) {
                travelInput.processed = true;
                addGameOverLog();
                pruneProcessedInputs();
                return this;
            }

            if (!opponent.isDead()) {
                pruneProcessedInputs();
                return this;
            }
        }

        if (destinationTile instanceof TreasureTile) {
            long readyAtMillis = travelInput.timestampMillis + TREASURE_TILE_TRAVEL_DELAY_MILLIS;
            if (timestampMillis < readyAtMillis) {
                pruneProcessedInputs();
                return this;
            }
        }

        if (!destinationTile.isPlayerRewarded()) {
            Reward reward = destinationTile.applyRewards();
            applyReward(reward);
        }

        player.moveTo(destinationHex);
        player.consumeTravelSupplies();
        travelInput.processed = true;

        if (isGameOver()) {
            addGameOverLog();
        }

        pruneProcessedInputs();
        return this;
    }

    public SeaTile whatsAt(Hex location) {
        return generatedHexagons.computeIfAbsent(location, hex -> tileFactory.create(hex, playerDetails(), generatedHexagons.values()));
    }

    public void attemptToTravel(Direction direction) {
        if (isGameOver() || isTreasureMovementDelayActive(System.currentTimeMillis())) {
            return;
        }

        inputEvents.add(InputEvent.travel(nextInputSequence++, System.currentTimeMillis(), direction));

        recalculateGameState(System.currentTimeMillis());
    }

    public void attemptToAttack() {
        attemptToAttack(Weapon.CUTLASS);
    }

    public void attemptToAttack(Weapon weapon) {
        if (isGameOver() || isTreasureMovementDelayActive(System.currentTimeMillis())) {
            return;
        }

        inputEvents.add(InputEvent.attack(nextInputSequence++, System.currentTimeMillis(), Objects.requireNonNull(weapon, "weapon")));
        recalculateGameState(System.currentTimeMillis());
    }

    public void attemptToFlee() {
        if (isGameOver() || isTreasureMovementDelayActive(System.currentTimeMillis())) {
            return;
        }

        inputEvents.add(InputEvent.flee(nextInputSequence++, System.currentTimeMillis()));
        recalculateGameState(System.currentTimeMillis());
    }

    public void startNewGame() {
        this.generatedHexagons.clear();
        this.log.clear();
        this.inputEvents.clear();
        this.nextInputSequence = 1L;

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

        return Collections.unmodifiableList(new ArrayList<>(attacksThisRound));
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

    private Optional<InputEvent> nextUnprocessedTravelEvent() {
        for (InputEvent inputEvent : inputEvents) {
            if (!inputEvent.processed && inputEvent.type == InputType.TRAVEL) {
                return Optional.of(inputEvent);
            }
        }
        return Optional.empty();
    }

    private Optional<InputEvent> nextUnprocessedActionInputAfter(long sequence) {
        for (InputEvent inputEvent : inputEvents) {
            if (!inputEvent.processed && inputEvent.sequence > sequence && inputEvent.type != InputType.TRAVEL) {
                return Optional.of(inputEvent);
            }
        }
        return Optional.empty();
    }

    private void markActionInputsBefore(long sequence) {
        for (InputEvent inputEvent : inputEvents) {
            if (!inputEvent.processed && inputEvent.sequence < sequence && inputEvent.type != InputType.TRAVEL) {
                inputEvent.processed = true;
            }
        }
    }

    private void markTravelInputsAfter(long sequence) {
        for (InputEvent inputEvent : inputEvents) {
            if (!inputEvent.processed && inputEvent.sequence > sequence && inputEvent.type == InputType.TRAVEL) {
                inputEvent.processed = true;
            }
        }
    }

    private void markUnprocessedActionInputsProcessed() {
        for (InputEvent inputEvent : inputEvents) {
            if (!inputEvent.processed && inputEvent.type != InputType.TRAVEL) {
                inputEvent.processed = true;
            }
        }
    }

    private void markAllUnprocessedInputsProcessed() {
        for (InputEvent inputEvent : inputEvents) {
            if (!inputEvent.processed) {
                inputEvent.processed = true;
            }
        }
    }

    private void pruneProcessedInputs() {
        inputEvents.removeIf(inputEvent -> inputEvent.processed);
    }

    private boolean isTreasureMovementDelayActive(long timestampMillis) {
        Optional<InputEvent> nextTravelInput = nextUnprocessedTravelEvent();
        if (nextTravelInput.isEmpty()) {
            return false;
        }

        InputEvent travelInput = nextTravelInput.get();
        SeaTile destinationTile = whatsAt(travelInput.resolveDestination(player.position()));
        if (!(destinationTile instanceof TreasureTile)) {
            return false;
        }

        long readyAtMillis = travelInput.timestampMillis + TREASURE_TILE_TRAVEL_DELAY_MILLIS;
        return timestampMillis < readyAtMillis;
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

    private enum InputType {
        TRAVEL,
        ATTACK,
        FLEE
    }

    private static final class InputEvent {
        private final InputType type;
        private final long sequence;
        private final long timestampMillis;
        private final Direction direction;
        private final Weapon weapon;
        private boolean processed;
        private Hex resolvedDestination;
        private boolean destinationRevealed;

        private InputEvent(InputType type, long sequence, long timestampMillis, Direction direction, Weapon weapon) {
            this.type = type;
            this.sequence = sequence;
            this.timestampMillis = timestampMillis;
            this.direction = direction;
            this.weapon = weapon;
        }

        private static InputEvent travel(long sequence, long timestampMillis, Direction direction) {
            return new InputEvent(InputType.TRAVEL, sequence, timestampMillis, direction, null);
        }

        private static InputEvent attack(long sequence, long timestampMillis, Weapon weapon) {
            return new InputEvent(InputType.ATTACK, sequence, timestampMillis, null, weapon);
        }

        private static InputEvent flee(long sequence, long timestampMillis) {
            return new InputEvent(InputType.FLEE, sequence, timestampMillis, null, null);
        }

        private Hex resolveDestination(Hex from) {
            if (this.resolvedDestination == null) {
                this.resolvedDestination = this.direction.move(from);
            }
            return this.resolvedDestination;
        }
    }
}
