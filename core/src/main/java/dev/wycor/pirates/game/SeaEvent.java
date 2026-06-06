package dev.wycor.pirates.game;

import java.util.Random;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

public enum SeaEvent {
    NOTHING(capturedTreasures -> EmptyTile.generate()),
    ISLAND(capturedTreasures -> IslandTile.generate()),
    GIANT_SQUID(capturedTreasures -> MonsterTile.giantSquid()),
    SEAWEED_MONSTER(capturedTreasures -> MonsterTile.seaweedMonster()),
    PHOENIX(capturedTreasures -> MonsterTile.phoenix()),
    GHOST_SHIP(capturedTreasures -> MonsterTile.ghostShip()),
    PIRATE_SHIP(capturedTreasures -> MonsterTile.pirateShip()),
    EMERALD_OF_HOPE(capturedTreasures -> TreasureTile.emeraldOfHope()),
    GOLDEN_SWORD_OF_YR(capturedTreasures -> TreasureTile.goldenSwordOfYr()),
    KING_FLYNNS_ROYAL_SCEPTRE(capturedTreasures -> TreasureTile.kingFlynnsRoyalSceptre()),
    SACRED_ONYX_CROSS(capturedTreasures -> TreasureTile.sacredOnyxCross()),
    LOST_PEARL_OF_JEHVA(capturedTreasures -> TreasureTile.lostPearlOfJehva()),
    QUEEN_LATHAS_CROWN(capturedTreasures -> TreasureTile.queenLathasCrown()),
    RUBY_RING_OF_POWER(capturedTreasures -> TreasureTile.rubyRingOfPower()),
    SILVER_CHALICE_OF_AUNGE(capturedTreasures -> TreasureTile.silverChaliceOfAunge()),
    MURPHYS_CHEST_OF_GOLD(capturedTreasures -> TreasureTile.murphysChestOfGold()),
    QUEEN_LATHAS_NECKLACE(capturedTreasures -> TreasureTile.queenLathasNecklace());

    private final Function<? super EnumMap<Treasure, Boolean>, ? extends SeaTile> tileGenerator;

    SeaEvent(Function<? super EnumMap<Treasure, Boolean>, ? extends SeaTile> tileGenerator) {
        this.tileGenerator = tileGenerator;
    }

//    private final Function<PlayerDetails, PlayerDetails> eventResolver;
//
//    SeaEvent(Function<PlayerDetails, PlayerDetails> eventResolver) {
//        this.eventResolver = eventResolver;
//    }

    public static SeaEvent random(EnumMap<Treasure, Boolean> capturedTreasures) {
        int random = new Random().nextInt(100);
        if (random < 10) {
            return ISLAND;
        }
        if (random < 15) {
            return GIANT_SQUID;
        }
        if (random < 20) {
            return SEAWEED_MONSTER;
        }
        if (random < 25) {
            return PHOENIX;
        }
        if (random < 30) {
            return GHOST_SHIP;
        }
        if (random < 35) {
            return PIRATE_SHIP;
        }
        if (random < 50) {
            SeaEvent treasureEvent = randomUncapturedTreasureEvent(capturedTreasures);
            if (treasureEvent != null) {
                return treasureEvent;
            }
        }
        return NOTHING;
    }

    public static SeaEvent random() {
        EnumMap<Treasure, Boolean> capturedTreasures = new EnumMap<>(Treasure.class);
        for (Treasure treasure : Treasure.values()) {
            capturedTreasures.put(treasure, false);
        }
        return random(capturedTreasures);
    }

    public SeaTile generate(EnumMap<Treasure, Boolean> capturedTreasures) {
        return this.tileGenerator.apply(capturedTreasures);
    }

    public SeaTile generate() {
        EnumMap<Treasure, Boolean> capturedTreasures = new EnumMap<>(Treasure.class);
        for (Treasure treasure : Treasure.values()) {
            capturedTreasures.put(treasure, false);
        }
        return generate(capturedTreasures);
    }

    private static SeaEvent randomUncapturedTreasureEvent(EnumMap<Treasure, Boolean> capturedTreasures) {
        Treasure[] uncapturedTreasures = capturedTreasures.entrySet().stream()
            .filter(entry -> !entry.getValue())
            .map(Map.Entry::getKey)
            .toArray(Treasure[]::new);

        if (uncapturedTreasures.length == 0) {
            return null;
        }

        Treasure selectedTreasure = uncapturedTreasures[new Random().nextInt(uncapturedTreasures.length)];
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
