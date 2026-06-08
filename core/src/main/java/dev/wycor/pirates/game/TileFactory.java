package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.MonsterTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    private final Map<Hex, SeaTile> generatedTiles;

    public TileFactory() {
        this.generatedTiles = new HashMap<>();
    }

    public TileFactory(Random worldGenRandom) {
        this.generatedTiles = generateAllTiles(Objects.requireNonNull(worldGenRandom, "worldGenRandom"));
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
     * Returns a copy of all non-origin world tiles.
     */
    public Map<Hex, SeaTile> generate() {
        return new HashMap<>(this.generatedTiles);
    }

    private static Map<Hex, SeaTile> generateAllTiles(Random worldGenRandom) {
        HashMap<Hex, SeaTile> generated = new HashMap<>();

        ArrayList<Hex> tilesToGenerate = new ArrayList<>(217);

        Hex.ORIGIN.spiral(World.WORLD_LAYERS)
            .filter(World::isWithinWorld)
            .filter(hex -> !Hex.ORIGIN.equals(hex))
            .forEach(tilesToGenerate::add);

        Collections.shuffle(tilesToGenerate, worldGenRandom);

        int pointer = 0;
        for (int i = 0; i < ISLAND_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), IslandTile.generate());
            pointer++;
        }

        for (int i = 0; i < MONSTER_COUNT_PER_TYPE; i++) {
            List<Supplier<MonsterTile>> list = Arrays.asList(MonsterTile::giantSquid, MonsterTile::seaweedMonster, MonsterTile::phoenix, MonsterTile::ghostShip, MonsterTile::pirateShip);

            for (Supplier<MonsterTile> monsterTileSupplier : list) {
                generated.put(tilesToGenerate.get(pointer), monsterTileSupplier.get());
                pointer++;
            }
        }
        for (int i = 0; i < WHIRLPOOL_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), HazardTile.whirlpool());
            pointer++;
        }
        for (int i = 0; i < ICEBERG_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), HazardTile.iceberg());
            pointer++;
        }
        for (Treasure treasure : Treasure.values()) {
            generated.put(tilesToGenerate.get(pointer), TreasureTile.forTreasure(treasure));
            pointer++;
        }

        while (pointer < tilesToGenerate.size()) {
            generated.put(tilesToGenerate.get(pointer), EmptyTile.generate());
            pointer++;
        }

        return generated;
    }
}
