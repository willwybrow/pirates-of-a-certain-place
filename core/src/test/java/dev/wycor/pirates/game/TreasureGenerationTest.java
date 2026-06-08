package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureGenerationTest {

    private static final int RANDOM_SAMPLES_PER_COMBINATION = 300;

    @Test
    void randomSeaEventsNeverSelectAnUnavailableTreasure() {
        Random random = new Random(1L);
        forEachTreasureCombination(unavailableTreasures -> {
            for (int i = 0; i < RANDOM_SAMPLES_PER_COMBINATION; i++) {
                SeaEvent event = SeaEvent.random(unavailableTreasures, random);
                Optional<Treasure> generatedTreasure = SeaEvent.treasureFor(event);

                if (generatedTreasure.isPresent()) {
                    assertThat(unavailableTreasures.get(generatedTreasure.get()))
                        .as("event %s must not be selected when treasure is unavailable", event)
                        .isFalse();
                }
            }
        });
    }

    @Test
    void tileGenerationNeverProducesAnUnavailableTreasureTile() {
        TileFactory tileFactory = new TileFactory();
        Random random = new Random(2L);

        forEachTreasureCombination(unavailableTreasures -> {
            for (int i = 0; i < RANDOM_SAMPLES_PER_COMBINATION; i++) {
                SeaTile generatedTile = tileFactory.create(new Hex(i, -i), random, existingTilesFor(unavailableTreasures));
                SeaEvent pendingEvent = generatedTile.pendingEvent();
                Optional<Treasure> generatedTreasure = SeaEvent.treasureFor(pendingEvent);

                if (generatedTreasure.isPresent()) {
                    assertThat(unavailableTreasures.get(generatedTreasure.get()))
                        .as("tile event %s must not generate an unavailable treasure", pendingEvent)
                        .isFalse();
                }
            }
        });
    }

    @Test
    void tileGenerationNeverDuplicatesTreasureAlreadyPresentInExistingTiles() {
        TileFactory tileFactory = new TileFactory();
        Random random = new Random(3L);

        forEachTreasureCombination(unavailableTreasures -> {
            for (Treasure existingTreasure : Treasure.values()) {
                if (unavailableTreasures.get(existingTreasure)) {
                    continue;
                }

                SeaTile existingTile = treasureTileFor(existingTreasure);
                for (int i = 0; i < RANDOM_SAMPLES_PER_COMBINATION; i++) {
                    SeaTile generatedTile = tileFactory.create(new Hex(i, i + 1), random, List.of(existingTile));
                    Optional<Treasure> generatedTreasure = SeaEvent.treasureFor(generatedTile.pendingEvent());

                    assertThat(generatedTreasure)
                        .as("generator must not place duplicate treasure %s when already on map", existingTreasure)
                        .isNotEqualTo(Optional.of(existingTreasure));
                }
            }
        });
    }

    private static void forEachTreasureCombination(TreasureCombinationAssertion assertion) {
        Treasure[] treasures = Treasure.values();
        int combinationCount = 1 << treasures.length;

        for (int bitmask = 0; bitmask < combinationCount; bitmask++) {
            EnumMap<Treasure, Boolean> unavailableTreasures = new EnumMap<>(Treasure.class);
            for (int i = 0; i < treasures.length; i++) {
                boolean unavailable = (bitmask & (1 << i)) != 0;
                unavailableTreasures.put(treasures[i], unavailable);
            }
            assertion.assertFor(unavailableTreasures);
        }
    }

    private static List<SeaTile> existingTilesFor(EnumMap<Treasure, Boolean> unavailableTreasures) {
        java.util.ArrayList<SeaTile> existingTiles = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Treasure, Boolean> entry : unavailableTreasures.entrySet()) {
            if (entry.getValue()) {
                existingTiles.add(treasureTileFor(entry.getKey()));
            }
        }
        return existingTiles;
    }

    private static SeaTile treasureTileFor(Treasure treasure) {
        switch (treasure) {
            case EMERALD_OF_HOPE:
                return TreasureTile.emeraldOfHope();
            case GOLDEN_SWORD_OF_YR:
                return TreasureTile.goldenSwordOfYr();
            case KING_FLYNNS_ROYAL_SCEPTRE:
                return TreasureTile.kingFlynnsRoyalSceptre();
            case SACRED_ONYX_CROSS:
                return TreasureTile.sacredOnyxCross();
            case LOST_PEARL_OF_JEHVA:
                return TreasureTile.lostPearlOfJehva();
            case QUEEN_LATHAS_CROWN:
                return TreasureTile.queenLathasCrown();
            case RUBY_RING_OF_POWER:
                return TreasureTile.rubyRingOfPower();
            case SILVER_CHALICE_OF_AUNGE:
                return TreasureTile.silverChaliceOfAunge();
            case MURPHYS_CHEST_OF_GOLD:
                return TreasureTile.murphysChestOfGold();
            case QUEEN_LATHAS_NECKLACE:
                return TreasureTile.queenLathasNecklace();
            default:
                throw new IllegalStateException("Unexpected treasure: " + treasure);
        }
    }

    @FunctionalInterface
    private interface TreasureCombinationAssertion {
        void assertFor(EnumMap<Treasure, Boolean> unavailableTreasures);
    }
}
