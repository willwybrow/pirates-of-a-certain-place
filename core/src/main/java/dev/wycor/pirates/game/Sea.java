package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.*;
import java.util.stream.Stream;

public class Sea {
    private static final int MAX_LOG_LINES = 60;
    private static final int WORLD_LAYERS = 8;
    private static final long TREASURE_TILE_TRAVEL_DELAY_MILLIS = 1_000;

    private Player player;
    private final TileFactory tileFactory;

    private long processedInputTimestamp;
    private InputEvent activeTravelInput;
    private final HashSet<Long> ignoredTravelInputTimestamps = new HashSet<>();

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(500);
    private final ArrayDeque<String> log = new ArrayDeque<>();
    private final ArrayDeque<InputEvent> inputEvents = new ArrayDeque<>();

    public Sea(TileFactory tileFactory) {
        this.tileFactory = Objects.requireNonNull(tileFactory, "tileFactory");
        this.processedInputTimestamp = -1L;
        this.activeTravelInput = null;
        startNewGame(0L);
        recalculateGameState(0L);
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
        return gameEndStatus() != GameEndStatus.ONGOING;
    }

    public String gameOverMessage() {
        switch (gameEndStatus()) {
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

    public Optional<Hex> getPlayerDestination() {
        InputEvent travelInput = activeTravelInput != null ? activeTravelInput : nextUnprocessedTravelEvent().orElse(null);
        if (travelInput == null) {
            return Optional.empty();
        }

        return Optional.of(travelInput)
            .map(inputEvent -> inputEvent.direction.move(player.position()))
            .filter(this::isWithinWorld);
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

    public Sea recalculateGameState(long timestampMillis) {
        /*
        1. the player has already input the command to go to a tile (by clicking the direction button to sail there), setting headingTo
        2. the tile is generated (if it's not generated already)
        3. the tile is revealed (if it's not visible already)
        4. while there's a living enemy on the tile, and the intention is still to move there (fleeing is possible which cancels the movement) combat ensues
        5. once combat is null or over, rewards are granted
        6. the player's position is set to the new tile and the intended movement is wiped
         */

        Optional<InputEvent> nextStartNewGameInput = nextUnprocessedStartNewGameInput();
        if (nextStartNewGameInput.isPresent()) {
            processedInputTimestamp = Math.max(processedInputTimestamp, nextStartNewGameInput.get().timestampMillis);
            processStartNewGame();
            return this;
        }

        if (player == null) {
            return this;
        }

        if (isGameOver()) {
            markAllInputsProcessed();
            return this;
        }

        if (activeTravelInput == null) {
            activeTravelInput = nextUnprocessedTravelEvent().orElse(null);
            if (activeTravelInput != null) {
                processedInputTimestamp = Math.max(processedInputTimestamp, activeTravelInput.timestampMillis);
                markQueuedTravelInputsAfterAsIgnored(activeTravelInput.timestampMillis);
            }
        }

        if (activeTravelInput == null) {
            markAllInputsProcessed();
            return this;
        }

        InputEvent travelInput = activeTravelInput;

        Hex destinationHex = travelInput.direction.move(player.position());
        if (!isWithinWorld(destinationHex)) {
            activeTravelInput = null;
            return this;
        }

        SeaTile destinationTile = whatsAt(destinationHex);
        boolean wasSpied = destinationTile.isSpied();
        destinationTile.spy(); // 2. and 3. -- generate and reveal
        if (!wasSpied) {
            addLog("Set course " + travelInput.direction + " and spied " + destinationTile.pendingEvent().name() + ".");
        }

        Combatant opponent = destinationTile.getCombatant();

        if (opponent != null && !opponent.isDead()) {
            Optional<InputEvent> nextActionInput = nextUnprocessedActionInputAfter(travelInput.timestampMillis);
            if (nextActionInput.isEmpty()) {
                return this;
            }

            InputEvent actionInput = nextActionInput.get();
            processedInputTimestamp = actionInput.timestampMillis;

            if (actionInput.type == InputType.FLEE) {
                destinationTile.onPlayerFled();
                activeTravelInput = null;
                addLog("You broke off and stayed at " + player.position() + ".");
                return this;
            }

            resolveCombatRound(opponent, actionInput.weapon).forEach(this::addAttackLog);

            if (isGameOver()) {
                activeTravelInput = null;
                addGameOverLog();
                return this;
            }

            if (!opponent.isDead()) {
                return this;
            }
        }

        if (destinationTile instanceof TreasureTile && !destinationTile.isPlayerRewarded()) {
            long readyAtMillis = travelInput.timestampMillis + TREASURE_TILE_TRAVEL_DELAY_MILLIS;
            if (timestampMillis < readyAtMillis) {
                return this;
            }
        }

        if (!destinationTile.isPlayerRewarded()) {
            Reward reward = destinationTile.applyRewards();
            applyReward(reward);
        }

        player.moveTo(destinationHex);
        player.consumeTravelSupplies();
        activeTravelInput = null;

        if (isGameOver()) {
            addGameOverLog();
        }
        return this;
    }

    public SeaTile whatsAt(Hex location) {
        if (!isWithinWorld(location)) {
            return EmptyTile.generate().spy();
        }

        return generatedHexagons.computeIfAbsent(location, hex -> tileFactory.create(hex, playerDetails(), generatedHexagons.values()));
    }

    public void attemptToTravel(Direction direction, long timestampMillis) {
        if (!submitInputEvent(InputEvent.travel(timestampMillis, direction))) {
            return;
        }

        recalculateGameState(timestampMillis);
    }

    public void attemptToAttack(long timestampMillis) {
        attemptToAttack(Weapon.CUTLASS, timestampMillis);
    }

    public void attemptToAttack(Weapon weapon, long timestampMillis) {
        if (!submitInputEvent(InputEvent.attack(timestampMillis, Objects.requireNonNull(weapon, "weapon")))) {
            return;
        }

        recalculateGameState(timestampMillis);
    }

    public void attemptToFlee(long timestampMillis) {
        if (!submitInputEvent(InputEvent.flee(timestampMillis))) {
            return;
        }

        recalculateGameState(timestampMillis);
    }

    public Stream<Hex> walkTheSpiral(int layers) {
        return this.generatedHexagons.keySet().stream();
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
            if (inputEvent.timestampMillis > processedInputTimestamp
                && inputEvent.type == InputType.TRAVEL
                && !ignoredTravelInputTimestamps.contains(inputEvent.timestampMillis)) {
                return Optional.of(inputEvent);
            }
        }
        return Optional.empty();
    }

    private Optional<InputEvent> nextUnprocessedStartNewGameInput() {
        for (InputEvent inputEvent : inputEvents) {
            if (inputEvent.timestampMillis > processedInputTimestamp && inputEvent.type == InputType.START_NEW_GAME) {
                return Optional.of(inputEvent);
            }
        }
        return Optional.empty();
    }

    private Optional<InputEvent> nextUnprocessedActionInputAfter(long timestampMillis) {
        for (InputEvent inputEvent : inputEvents) {
            if (inputEvent.timestampMillis > processedInputTimestamp
                && inputEvent.timestampMillis > timestampMillis
                && inputEvent.type != InputType.TRAVEL
                && inputEvent.type != InputType.START_NEW_GAME) {
                return Optional.of(inputEvent);
            }
        }
        return Optional.empty();
    }

    private void markQueuedTravelInputsAfterAsIgnored(long timestampMillis) {
        for (InputEvent inputEvent : inputEvents) {
            if (inputEvent.type == InputType.TRAVEL && inputEvent.timestampMillis > timestampMillis) {
                ignoredTravelInputTimestamps.add(inputEvent.timestampMillis);
            }
        }
    }

    private void markAllInputsProcessed() {
        if (inputEvents.isEmpty()) {
            return;
        }
        InputEvent latest = inputEvents.peekLast();
        if (latest.timestampMillis > processedInputTimestamp) {
            processedInputTimestamp = latest.timestampMillis;
        }
    }

    private boolean submitInputEvent(InputEvent inputEvent) {
        if (inputEvent.type != InputType.START_NEW_GAME) {
            if (isGameOver()) {
                return false;
            }
            if (isTreasureMovementDelayActive(inputEvent.timestampMillis)) {
                return false;
            }
        }

        InputEvent latestSubmittedInput = inputEvents.peekLast();
        if (latestSubmittedInput != null && inputEvent.timestampMillis <= latestSubmittedInput.timestampMillis) {
            return false;
        }

        inputEvents.addLast(inputEvent);
        return true;
    }

    public void startNewGame(long timestampMillis) {
        submitInputEvent(InputEvent.startNewGame(timestampMillis));
    }

    private void processStartNewGame() {
        this.generatedHexagons.clear();
        this.log.clear();
        this.inputEvents.clear();
        this.processedInputTimestamp = -1L;
        this.activeTravelInput = null;
        this.ignoredTravelInputTimestamps.clear();

        this.player = new Player(Hex.ORIGIN);
        this.generatedHexagons.put(Hex.ORIGIN, SeaTile.startingSquare());
        preGenerateWorld();
        addLog("Set sail from home waters.");
    }

    private boolean isTreasureMovementDelayActive(long timestampMillis) {
        Optional<InputEvent> nextTravelInput = nextUnprocessedTravelEvent();
        if (nextTravelInput.isEmpty()) {
            return false;
        }

        InputEvent travelInput = nextTravelInput.get();
        SeaTile destinationTile = whatsAt(travelInput.direction.move(player.position()));
        if (!(destinationTile instanceof TreasureTile) || destinationTile.isPlayerRewarded()) {
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
        String message = gameOverMessage();
        if (!message.isEmpty() && (log.isEmpty() || !message.equals(log.peekFirst()))) {
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

    private void preGenerateWorld() {
        ArrayList<Hex> worldHexes = new ArrayList<>();
        Hex.ORIGIN.spiral(WORLD_LAYERS)
            .filter(this::isWithinWorld)
            .filter(hex -> !Hex.ORIGIN.equals(hex))
            .forEach(worldHexes::add);

        worldHexes.sort(Comparator.comparingInt(Hex::q).thenComparingInt(Hex::r));

        for (Hex hex : worldHexes) {
            generatedHexagons.computeIfAbsent(hex, candidate -> tileFactory.create(candidate, playerDetails(), generatedHexagons.values()));
        }

        ensureAllTreasuresPlaced(worldHexes);
    }

    private void ensureAllTreasuresPlaced(List<Hex> worldHexes) {
        EnumSet<Treasure> placedTreasures = EnumSet.noneOf(Treasure.class);
        for (SeaTile tile : generatedHexagons.values()) {
            SeaEvent.treasureFor(tile.pendingEvent()).ifPresent(placedTreasures::add);
            SeaEvent.treasureFor(tile.completedEvent()).ifPresent(placedTreasures::add);
        }

        ArrayList<Treasure> missingTreasures = new ArrayList<>();
        for (Treasure treasure : Treasure.values()) {
            if (!placedTreasures.contains(treasure)) {
                missingTreasures.add(treasure);
            }
        }

        if (missingTreasures.isEmpty()) {
            return;
        }

        Iterator<Treasure> missingIterator = missingTreasures.iterator();
        for (Hex hex : worldHexes) {
            if (!missingIterator.hasNext()) {
                break;
            }

            SeaTile currentTile = generatedHexagons.get(hex);
            if (currentTile instanceof TreasureTile) {
                continue;
            }

            generatedHexagons.put(hex, TreasureTile.forTreasure(missingIterator.next()));
        }
    }

    private boolean isWithinWorld(Hex hex) {
        return Math.abs(hex.q()) <= WORLD_LAYERS
            && Math.abs(hex.r()) <= WORLD_LAYERS
            && Math.abs(hex.s()) <= WORLD_LAYERS;
    }

    private enum InputType {
        START_NEW_GAME,
        TRAVEL,
        ATTACK,
        FLEE
    }

    private enum GameEndStatus {
        ONGOING,
        DEFEATED,
        STARVED,
        TREASURES_FOUND
    }

    private static final class InputEvent {
        private final InputType type;
        private final long timestampMillis;
        private final Direction direction;
        private final Weapon weapon;

        private InputEvent(InputType type, long timestampMillis, Direction direction, Weapon weapon) {
            this.type = type;
            this.timestampMillis = timestampMillis;
            this.direction = direction;
            this.weapon = weapon;
        }

        private static InputEvent travel(long timestampMillis, Direction direction) {
            return new InputEvent(InputType.TRAVEL, timestampMillis, direction, null);
        }

        private static InputEvent startNewGame(long timestampMillis) {
            return new InputEvent(InputType.START_NEW_GAME, timestampMillis, null, null);
        }

        private static InputEvent attack(long timestampMillis, Weapon weapon) {
            return new InputEvent(InputType.ATTACK, timestampMillis, null, weapon);
        }

        private static InputEvent flee(long timestampMillis) {
            return new InputEvent(InputType.FLEE, timestampMillis, null, null);
        }

    }
}
