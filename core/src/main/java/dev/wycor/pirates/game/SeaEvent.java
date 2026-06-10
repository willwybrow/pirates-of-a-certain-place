package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.ItemTile;
import dev.wycor.pirates.game.tile.IslandTile;
import dev.wycor.pirates.game.tile.MonsterTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;

import java.util.Random;
import java.util.EnumMap;
import java.util.function.Function;
import java.util.Map;
import java.util.Optional;

public enum SeaEvent {
    NOTHING(random -> EmptyTile.generate()),
    ISLAND(random -> IslandTile.generate()),
    ICEBERG(random -> HazardTile.iceberg()),
    WHIRLPOOL(random -> HazardTile.whirlpool()),
    GIANT_SQUID(MonsterTile::giantSquid),
    SEAWEED_MONSTER(MonsterTile::seaweedMonster),
    PHOENIX(MonsterTile::phoenix),
    GHOST_SHIP(MonsterTile::ghostShip),
    PIRATE_SHIP(MonsterTile::pirateShip),
    TAR(random -> ItemTile.tar()),
    MAP(random -> ItemTile.map()),
    SEXTANT(random -> ItemTile.sextant()),
    SPYGLASS(random -> ItemTile.spyglass()),
    EMERALD_OF_HOPE(random -> TreasureTile.emeraldOfHope()),
    GOLDEN_SWORD_OF_YR(random -> TreasureTile.goldenSwordOfYr()),
    KING_FLYNNS_ROYAL_SCEPTRE(random -> TreasureTile.kingFlynnsRoyalSceptre()),
    SACRED_ONYX_CROSS(random -> TreasureTile.sacredOnyxCross()),
    LOST_PEARL_OF_JEHVA(random -> TreasureTile.lostPearlOfJehva()),
    QUEEN_LATHAS_CROWN(random -> TreasureTile.queenLathasCrown()),
    RUBY_RING_OF_POWER(random -> TreasureTile.rubyRingOfPower()),
    SILVER_CHALICE_OF_AUNGE(random -> TreasureTile.silverChaliceOfAunge()),
    MURPHYS_CHEST_OF_GOLD(random -> TreasureTile.murphysChestOfGold()),
    QUEEN_LATHAS_NECKLACE(random -> TreasureTile.queenLathasNecklace());

    private final Function<Random, ? extends SeaTile> tileGenerator;

    SeaEvent(Function<Random, ? extends SeaTile> tileGenerator) {
        this.tileGenerator = tileGenerator;
    }

    public static SeaEvent random(EnumMap<Treasure, Boolean> unavailableTreasures, Random random) {
        int roll = random.nextInt(100);
        if (roll < 10) {
            return ISLAND;
        }
        if (roll < 15) {
            return GIANT_SQUID;
        }
        if (roll < 20) {
            return SEAWEED_MONSTER;
        }
        if (roll < 25) {
            return PHOENIX;
        }
        if (roll < 30) {
            return GHOST_SHIP;
        }
        if (roll < 35) {
            return PIRATE_SHIP;
        }
        if (roll < 40) {
            return ICEBERG;
        }
        if (roll < 45) {
            return WHIRLPOOL;
        }
        if (roll < 60) {
            SeaEvent treasureEvent = randomAvailableTreasureEvent(unavailableTreasures, random);
            if (treasureEvent != null) {
                return treasureEvent;
            }
        }
        return NOTHING;
    }

    public SeaTile generate(Random monsterRandom) {
        return this.tileGenerator.apply(monsterRandom);
    }

    public static Optional<Treasure> treasureFor(SeaEvent seaEvent) {
        switch (seaEvent) {
            case EMERALD_OF_HOPE:
                return Optional.of(Treasure.EMERALD_OF_HOPE);
            case GOLDEN_SWORD_OF_YR:
                return Optional.of(Treasure.GOLDEN_SWORD_OF_YR);
            case KING_FLYNNS_ROYAL_SCEPTRE:
                return Optional.of(Treasure.KING_FLYNNS_ROYAL_SCEPTRE);
            case SACRED_ONYX_CROSS:
                return Optional.of(Treasure.SACRED_ONYX_CROSS);
            case LOST_PEARL_OF_JEHVA:
                return Optional.of(Treasure.LOST_PEARL_OF_JEHVA);
            case QUEEN_LATHAS_CROWN:
                return Optional.of(Treasure.QUEEN_LATHAS_CROWN);
            case RUBY_RING_OF_POWER:
                return Optional.of(Treasure.RUBY_RING_OF_POWER);
            case SILVER_CHALICE_OF_AUNGE:
                return Optional.of(Treasure.SILVER_CHALICE_OF_AUNGE);
            case MURPHYS_CHEST_OF_GOLD:
                return Optional.of(Treasure.MURPHYS_CHEST_OF_GOLD);
            case QUEEN_LATHAS_NECKLACE:
                return Optional.of(Treasure.QUEEN_LATHAS_NECKLACE);
            default:
                return Optional.empty();
        }
    }

    private static SeaEvent randomAvailableTreasureEvent(EnumMap<Treasure, Boolean> unavailableTreasures, Random random) {
        Treasure[] availableTreasures = unavailableTreasures.entrySet().stream()
            .filter(entry -> !entry.getValue())
            .map(Map.Entry::getKey)
            .toArray(Treasure[]::new);

        if (availableTreasures.length == 0) {
            return null;
        }

        Treasure selectedTreasure = availableTreasures[random.nextInt(availableTreasures.length)];
        switch (selectedTreasure) {
            case EMERALD_OF_HOPE:
                return EMERALD_OF_HOPE;
            case GOLDEN_SWORD_OF_YR:
                return GOLDEN_SWORD_OF_YR;
            case KING_FLYNNS_ROYAL_SCEPTRE:
                return KING_FLYNNS_ROYAL_SCEPTRE;
            case SACRED_ONYX_CROSS:
                return SACRED_ONYX_CROSS;
            case LOST_PEARL_OF_JEHVA:
                return LOST_PEARL_OF_JEHVA;
            case QUEEN_LATHAS_CROWN:
                return QUEEN_LATHAS_CROWN;
            case RUBY_RING_OF_POWER:
                return RUBY_RING_OF_POWER;
            case SILVER_CHALICE_OF_AUNGE:
                return SILVER_CHALICE_OF_AUNGE;
            case MURPHYS_CHEST_OF_GOLD:
                return MURPHYS_CHEST_OF_GOLD;
            case QUEEN_LATHAS_NECKLACE:
                return QUEEN_LATHAS_NECKLACE;
            default:
                return null;
        }
    }
}
