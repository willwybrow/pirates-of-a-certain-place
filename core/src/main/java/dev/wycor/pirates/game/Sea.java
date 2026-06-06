package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.*;
import java.util.stream.Stream;

public class Sea {

    private final PlayerDetails playerDetails;

    private Hex headingTo; // null when still

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(500);
    private final ArrayDeque<String> log = new ArrayDeque<>();

    public Sea() {
        this.generatedHexagons.put(Hex.ORIGIN, SeaTile.startingSquare());
        this.playerDetails = new PlayerDetails(Hex.ORIGIN);
    }

    public Hex playerPosition() {
        return this.playerDetails.position();
    }

    public Optional<Hex> getDestination() {
        return Optional.ofNullable(headingTo).filter(playerPosition().neighbours()::contains);
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

        getDestination().ifPresent(headingTo -> {
            SeaTile destination = whatsAt(headingTo).spy(); // 2. and 3. -- generate and reveal

            Optional<Combat> combatInProgress = Optional.ofNullable(destination.getCombatEvent(playerDetails));

            if (combatInProgress.isPresent()) {
                return; // nothing to do until the combat is resolved
            }

            if (!destination.isPlayerRewarded()) {
                destination.applyRewards(playerDetails);
            }

        });

        return this;
    }

    public SeaTile whatsAt(Hex location) {
        return generatedHexagons.computeIfAbsent(location, hex -> SeaTile.random());
    }

    public void attemptToTravel(Direction direction) {
        if (getDestination().isEmpty()) {
            Hex headingFrom = playerDetails.position();
            this.headingTo = direction.move(headingFrom);

            SeaTile upcomingThing = whatsAt(headingTo).spy();
            log.addFirst("Travelled " + direction + " and spied " + upcomingThing.pendingEvent().name());
        }

        getGameState();
    }

    public void attemptToAttack() {

    }

    public Set<Hex> explored() {
        return generatedHexagons.keySet();
    }

    public Stream<Hex> walkTheSpiral(int layers) {
        Hex start = getDestination().orElseGet(this::playerPosition);
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
}
