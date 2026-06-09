package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.ItemTile;
import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.MonsterTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final int TAR_COUNT = 3;
    private static final int MAP_COUNT = 3;
    private static final int SEXTANT_COUNT = 4;
    private static final int SPYGLASS_COUNT = 4;

    private final Random worldGenRandom;


    public TileFactory(Random worldRandom) {
        this.worldGenRandom = worldRandom;
    }

    /**
     * Generates a mutable world filled with tiles for tracking the game state. Called once at game start.
     * @return A new world
     */
    public World generate() {
        return new World(this.generateAllTiles(World.WORLD_LAYERS));
    }

    private Map<Hex, SeaTile> generateAllTiles(int layers) {
        int hexCount = Hex.centredHexagonalNumber(layers);
        HashMap<Hex, SeaTile> generated = new HashMap<>();

        ArrayList<Hex> tilesToGenerate = new ArrayList<>(hexCount);

        Hex.ORIGIN.spiral(World.WORLD_LAYERS)
            .filter(World::isWithinWorld)
            .filter(hex -> !Hex.ORIGIN.equals(hex))
            .forEach(tilesToGenerate::add);

        Collections.shuffle(tilesToGenerate, this.worldGenRandom);

        int pointer = 0;
        generated.put(Hex.ORIGIN, SeaTile.startingSquare());

        for (int i = 0; i < ISLAND_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), IslandTile.generate());
            pointer++;
        }

        for (int i = 0; i < MONSTER_COUNT_PER_TYPE; i++) {
            List<Supplier<MonsterTile>> list = Arrays.asList(
                () -> MonsterTile.giantSquid(this.worldGenRandom),
                MonsterTile::seaweedMonster,
                () -> MonsterTile.phoenix(this.worldGenRandom),
                MonsterTile::ghostShip,
                () -> MonsterTile.pirateShip(this.worldGenRandom)
            );

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
        for (int i = 0; i < TAR_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), ItemTile.tar());
            pointer++;
        }
        for (int i = 0; i < MAP_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), ItemTile.map());
            pointer++;
        }
        for (int i = 0; i < SEXTANT_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), ItemTile.sextant());
            pointer++;
        }
        for (int i = 0; i < SPYGLASS_COUNT; i++) {
            generated.put(tilesToGenerate.get(pointer), ItemTile.spyglass());
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
