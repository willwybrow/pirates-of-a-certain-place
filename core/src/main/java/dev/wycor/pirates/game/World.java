package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The generated sea: every tile and its mutable in-game state.
 *
 * <p>The layout (which tile sits on which hex, where treasures are placed) is a pure function of
 * the world-generation random stream, which is itself derived from the game seed. Constructing a
 * {@code World} with the same seed always produces the same layout. Per-tile mutable state
 * ({@code spied}/{@code rewarded}/monster health, etc.) changes as the game is simulated and is
 * therefore part of the computed game state, not the durable input log.
 */
final class World {
    static final int WORLD_LAYERS = 7;

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(250);

    World(Map<Hex, SeaTile> tiles) {
        this.generatedHexagons.putAll(tiles);
    }

    SeaTile whatsAt(Hex location) {
        if (!isWithinWorld(location)) {
            return EmptyTile.generate().spy();
        }

        SeaTile tile = generatedHexagons.get(location);
        if (tile != null) {
            return tile;
        }

        return EmptyTile.generate().spy();
    }

    /** Snapshot of all currently generated hexes, in a deterministic order. */
    List<Hex> generatedHexes() {
        ArrayList<Hex> hexes = new ArrayList<>(generatedHexagons.keySet());
        hexes.sort(Comparator.comparingInt(Hex::q).thenComparingInt(Hex::r));
        return hexes;
    }

    /**
     * Hides the given treasure under a random unexplored (un-spied) tile, replacing whatever was
     * there. Returns {@code true} if a hiding spot was found, {@code false} if every tile is already
     * explored (in which case the treasure cannot be re-hidden).
     */
    boolean rehideTreasureUnderUnexploredTile(Treasure treasure, Random random) {
        ArrayList<Hex> unexploredHexes = new ArrayList<>();
        for (Hex hex : generatedHexes()) {
            if (!generatedHexagons.get(hex).isSpied()) {
                unexploredHexes.add(hex);
            }
        }

        if (unexploredHexes.isEmpty()) {
            return false;
        }

        Hex chosen = unexploredHexes.get(random.nextInt(unexploredHexes.size()));
        generatedHexagons.put(chosen, TreasureTile.forTreasure(treasure));
        return true;
    }

    static boolean isWithinWorld(Hex hex) {
        return Math.abs(hex.q()) <= WORLD_LAYERS
            && Math.abs(hex.r()) <= WORLD_LAYERS
            && Math.abs(hex.s()) <= WORLD_LAYERS;
    }
}
