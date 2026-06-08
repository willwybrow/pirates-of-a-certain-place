package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Pure simulation rules. Given a seed and an ordered list of inputs, the engine folds each input
 * over a {@link GameState} to determine the exact game state. It holds no state of its own, so the
 * same seed and inputs always yield an identical result.
 */
final class GameEngine {
    private static final long TILE_TRAVEL_DELAY_MILLIS = 1_000;
    private static final int ICEBERG_DAMAGE = 20;

    private final TileFactory tileFactory;

    GameEngine(TileFactory tileFactory) {
        this.tileFactory = tileFactory;
    }

    /** Builds a fresh state from the seed and folds every input due at or before the timestamp. */
    GameState replay(long seed, List<GameInput> inputs, long upToTimestampMillis) {
        GameState state = newGame(seed);
        for (GameInput input : inputs) {
            if (input.timestampMillis() <= upToTimestampMillis) {
                applyInput(state, input);
            }
        }
        advance(state, upToTimestampMillis);
        return state;
    }

    /** Creates the opening state for a seed and logs the starting line. */
    GameState newGame(long seed) {
        GameState state = new GameState(seed, tileFactory);
        state.addLog("Set sail from home waters.");
        return state;
    }

    /**
     * Applies a single input to the running state, then advances time-gated consequences. Used both
     * by full replay and by the incremental live cache.
     */
    void applyInput(GameState state, GameInput input) {
        processGameplayInput(state, input);
        advance(state, input.timestampMillis());
    }

    /** Advances time-dependent consequences (travel completion, end-of-game logging) to a timestamp. */
    void advance(GameState state, long timestampMillis) {
        resolveActiveTravel(state, timestampMillis);
        if (state.isGameOver()) {
            addGameOverLog(state);
        }
    }

    private void processGameplayInput(GameState state, GameInput input) {
        if (state.isGameOver()) {
            return;
        }

        if (input instanceof GameInput.Travel) {
            processTravelInput(state, (GameInput.Travel) input);
            return;
        }

        Optional<Monster> liveOpponent = state.liveOpponentAtDestination();
        if (liveOpponent.isEmpty()) {
            return;
        }
        Monster opponent = liveOpponent.get();

        if (input instanceof GameInput.Attack) {
            processAttackInput(state, (GameInput.Attack) input, opponent);
            return;
        }

        if (input instanceof GameInput.Flee) {
            processFleeInput(state);
        }
    }

    private void processTravelInput(GameState state, GameInput.Travel travel) {
        if (state.outstandingTravel().isPresent()) {
            return;
        }

        Hex origin = state.player().position();
        Hex destination = travel.direction().move(origin);
        state.beginTravel(destination, travel.timestampMillis());
    }

    private void processAttackInput(GameState state, GameInput.Attack attack, Monster opponent) {
        Weapon weapon = attack.weapon();
        if (!state.player().canWield(weapon)) {
            return;
        }
        if (!state.player().consumeAmmunition(weapon)) {
            state.addLog("Out of ammunition for " + weapon.displayName() + ".");
            return;
        }

        resolveCombatRound(state, opponent, weapon).forEach(resolved -> addAttackLog(state, resolved));
    }

    private void processFleeInput(GameState state) {
        Optional<GameState.ActiveTravel> outstandingTravel = state.outstandingTravel();
        if (outstandingTravel.isEmpty()) {
            return;
        }

        SeaTile destinationTile = state.world().whatsAt(outstandingTravel.get().destination());
        destinationTile.onPlayerFled();
        state.cancelActiveTravel();
        state.addLog("You broke off and stayed at " + state.player().position() + ".");
    }

    private List<Attack> resolveCombatRound(GameState state, Monster opponent, Weapon weapon) {
        Player player = state.player();
        ArrayList<Attack> attacksThisRound = new ArrayList<>(2);

        Attack playerAttack = new Attack(player, opponent, weapon);
        attacksThisRound.add(opponent.receiveAttack(playerAttack, state.combatRandom()));

        if (!opponent.isDead()) {
            attacksThisRound.add(opponent.strike(player, state.combatRandom()));
        }

        return attacksThisRound;
    }

    private void resolveActiveTravel(GameState state, long timestampMillis) {
        Optional<GameState.ActiveTravel> outstandingTravel = state.outstandingTravel();
        if (outstandingTravel.isEmpty()) {
            return;
        }

        GameState.ActiveTravel travel = outstandingTravel.get();
        Hex destinationHex = travel.destination();
        if (!World.isWithinWorld(destinationHex)) {
            state.cancelActiveTravel();
            return;
        }

        SeaTile destinationTile = state.world().whatsAt(destinationHex);
        boolean wasSpied = destinationTile.isSpied();
        destinationTile.spy();
        if (!wasSpied) {
            state.addLog("Set course " + directionTo(state.player().position(), destinationHex)
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
            resolveHazard(state, (HazardTile) destinationTile);
        }

        if (!destinationTile.isPlayerRewarded()) {
            applyReward(state, destinationTile.applyRewards());
        }

        state.player().moveTo(destinationHex);
        state.player().consumeTravelSupplies();
        state.clearActiveTravel();
    }

    private void resolveHazard(GameState state, HazardTile hazardTile) {
        switch (hazardTile.hazard()) {
            case ICEBERG:
                resolveIceberg(state);
                break;
            case WHIRLPOOL:
                resolveWhirlpool(state);
                break;
            default:
                break;
        }
        hazardTile.trigger();
    }

    private void resolveIceberg(GameState state) {
        state.player().takeDamage(ICEBERG_DAMAGE);
        state.addLog("An iceberg gouged the hull for " + ICEBERG_DAMAGE + " damage.");
    }

    private void resolveWhirlpool(GameState state) {
        List<Treasure> capturedTreasures = state.player().capturedTreasureList();
        if (capturedTreasures.isEmpty()) {
            state.addLog("A whirlpool churned past, but you had no treasure to lose.");
            return;
        }

        Treasure lostTreasure = capturedTreasures.get(state.hazardRandom().nextInt(capturedTreasures.size()));
        boolean rehidden = state.world().rehideTreasureUnderUnexploredTile(lostTreasure, state.hazardRandom());
        if (!rehidden) {
            state.addLog("A whirlpool churned past, but there was nowhere left to hide your treasure.");
            return;
        }

        state.player().loseTreasure(lostTreasure);
        state.addLog("A whirlpool swallowed the " + lostTreasure.displayName() + " and hid it anew.");
    }

    private void applyReward(GameState state, Reward reward) {
        Player player = state.player();
        if (reward.health() != 0) {
            player.heal(reward.health());
        }
        if (reward.food() != 0) {
            player.restock(reward.food());
        }
        reward.ammunitionByWeapon().forEach(player::restockAmmunition);
        if (reward.treasure() != null) {
            player.captureTreasure(reward.treasure());
            state.addLog("Recovered " + reward.treasure().displayName() + ".");
        }
    }

    private void addAttackLog(GameState state, Attack attack) {
        state.addLog(attack.initiator().name() + " hit " + attack.defender().name()
            + " for " + attack.actualDamage() + ".");
    }

    private void addGameOverLog(GameState state) {
        String message = gameOverMessage(state.gameEndStatus());
        if (!message.isEmpty() && (state.logIsEmpty() || !message.equals(state.peekLatestLog()))) {
            state.addLog(message);
        }
    }

    static String gameOverMessage(GameState.GameEndStatus status) {
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

    private static Direction directionTo(Hex from, Hex destination) {
        for (Direction direction : Direction.values()) {
            if (direction.move(from).equals(destination)) {
                return direction;
            }
        }
        return null;
    }
}
