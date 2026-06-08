package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.MonsterTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Creates tiles for world generation.
 *
 * <p>The default implementation creates a fixed composition (islands, monsters, hazards, and one
 * of each treasure) and places non-empty tiles near the outer world bounds.
 */
public class TileFactory {

    private static final int ISLAND_COUNT = 8;
    private static final int MONSTER_COUNT_PER_TYPE = 6;
    private static final int WHIRLPOOL_COUNT = 2;
    private static final int ICEBERG_COUNT = 2;
    private static final int MONSTER_TYPE_COUNT = 5;
    private static final int NON_EMPTY_TILE_COUNT = ISLAND_COUNT
        + MONSTER_COUNT_PER_TYPE * MONSTER_TYPE_COUNT
        + WHIRLPOOL_COUNT
        + ICEBERG_COUNT
        + Treasure.values().length;

    private final Random worldGenRandom;

    public TileFactory(Random worldGenRandom) {
        this.worldGenRandom = Objects.requireNonNull(worldGenRandom, "worldGenRandom");
    }

    /**
     * Creates a world-scoped tile factory backed by the seed-derived world generation stream.
     */
    TileFactory forWorld(Random worldGenRandom) {
        if (this.getClass() == TileFactory.class) {
            return new TileFactory(worldGenRandom);
        }
        return this;
    }

    /**
     * Generates every non-origin tile in the world in one deterministic pass.
     */
    public Map<Hex, SeaTile> generate(List<Hex> worldHexes) {
        if (this.getClass() != TileFactory.class) {
            return generateUsingLegacyCreate(worldHexes);
        }

        HashMap<Hex, SeaTile> generated = new HashMap<>(worldHexes.size());
        for (Hex hex : worldHexes) {
            generated.put(hex, EmptyTile.generate());
        }

        ArrayList<Hex> outerFirstHexes = new ArrayList<>(worldHexes);
        Collections.shuffle(outerFirstHexes, this.worldGenRandom);
        outerFirstHexes.sort(Comparator.comparingInt(TileFactory::distanceFromOrigin).reversed());

        ArrayList<SeaTile> nonEmptyTiles = nonEmptyTiles();
        Collections.shuffle(nonEmptyTiles, this.worldGenRandom);

        int placements = Math.min(NON_EMPTY_TILE_COUNT, Math.min(outerFirstHexes.size(), nonEmptyTiles.size()));
        for (int i = 0; i < placements; i++) {
            generated.put(outerFirstHexes.get(i), nonEmptyTiles.get(i));
        }

        return generated;
    }

    /**
     * Legacy per-hex hook retained for tests that provide hand-authored world layouts.
     */
    public SeaTile create(Hex hex, Collection<SeaTile> existingTiles) {
        EnumMap<Treasure, Boolean> unavailableTreasures = new EnumMap<>(Treasure.class);
        for (Treasure treasure : Treasure.values()) {
            unavailableTreasures.put(treasure, false);
        }

        for (SeaTile existingTile : existingTiles) {
            Optional<Treasure> pendingTreasure = SeaEvent.treasureFor(existingTile.pendingEvent());
            pendingTreasure.ifPresent(treasure -> unavailableTreasures.put(treasure, true));

            Optional<Treasure> completedTreasure = SeaEvent.treasureFor(existingTile.completedEvent());
            completedTreasure.ifPresent(treasure -> unavailableTreasures.put(treasure, true));
        }

        return SeaTile.random(unavailableTreasures, this.worldGenRandom);
    }

    private Map<Hex, SeaTile> generateUsingLegacyCreate(List<Hex> worldHexes) {
        HashMap<Hex, SeaTile> generated = new HashMap<>(worldHexes.size());
        for (Hex hex : worldHexes) {
            generated.put(hex, create(hex, generated.values()));
        }
        return generated;
    }

    private static int distanceFromOrigin(Hex hex) {
        return Math.max(Math.abs(hex.q()), Math.max(Math.abs(hex.r()), Math.abs(hex.s())));
    }

    private static ArrayList<SeaTile> nonEmptyTiles() {
        ArrayList<SeaTile> tiles = new ArrayList<>(NON_EMPTY_TILE_COUNT);

        addTiles(tiles, ISLAND_COUNT, IslandTile::generate);
        addTiles(tiles, MONSTER_COUNT_PER_TYPE, MonsterTile::giantSquid);
        addTiles(tiles, MONSTER_COUNT_PER_TYPE, MonsterTile::seaweedMonster);
        addTiles(tiles, MONSTER_COUNT_PER_TYPE, MonsterTile::phoenix);
        addTiles(tiles, MONSTER_COUNT_PER_TYPE, MonsterTile::ghostShip);
        addTiles(tiles, MONSTER_COUNT_PER_TYPE, MonsterTile::pirateShip);
        addTiles(tiles, WHIRLPOOL_COUNT, HazardTile::whirlpool);
        addTiles(tiles, ICEBERG_COUNT, HazardTile::iceberg);

        for (Treasure treasure : Treasure.values()) {
            tiles.add(TreasureTile.forTreasure(treasure));
        }

        return tiles;
    }

    private static void addTiles(ArrayList<SeaTile> tiles, int count, Supplier<? extends SeaTile> supplier) {
        for (int i = 0; i < count; i++) {
            tiles.add(supplier.get());
        }
    }
}
