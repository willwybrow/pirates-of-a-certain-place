package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureGenerationTest {

    @Test
    void generatedWorldHasOriginalFixedComposition() {
        Sea sea = new Sea(new TileFactory());
        sea.startNewGame(42L, 0L);

        EnumMap<SeaEvent, Integer> counts = countPendingEvents(sea);

        assertThat(sea.generatedHexes()).hasSize(169);
        assertThat(counts.getOrDefault(SeaEvent.ISLAND, 0)).isEqualTo(8);
        assertThat(counts.getOrDefault(SeaEvent.GIANT_SQUID, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.SEAWEED_MONSTER, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.PHOENIX, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.GHOST_SHIP, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.PIRATE_SHIP, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.WHIRLPOOL, 0)).isEqualTo(2);
        assertThat(counts.getOrDefault(SeaEvent.ICEBERG, 0)).isEqualTo(2);

        for (Treasure treasure : Treasure.values()) {
            assertThat(counts.getOrDefault(SeaEvent.valueOf(treasure.name()), 0))
                .as("treasure %s should appear exactly once", treasure)
                .isEqualTo(1);
        }

        assertThat(counts.getOrDefault(SeaEvent.NOTHING, 0)).isEqualTo(117);
    }

    @Test
    void generatedWorldBiasesNonEmptyTilesTowardTheOuterBounds() {
        Sea sea = new Sea(new TileFactory());
        sea.startNewGame(99L, 0L);

        ArrayList<Integer> nonEmptyDistances = new ArrayList<>();
        for (Hex hex : sea.generatedHexes()) {
            if (Hex.ORIGIN.equals(hex)) {
                continue;
            }

            SeaTile tile = sea.whatsAt(hex);
            if (tile.pendingEvent() != SeaEvent.NOTHING) {
                nonEmptyDistances.add(distanceFromOrigin(hex));
            }
        }

        assertThat(nonEmptyDistances).hasSize(52);
        assertThat(nonEmptyDistances).allMatch(distance -> distance >= World.WORLD_LAYERS - 1);
    }

    private static EnumMap<SeaEvent, Integer> countPendingEvents(Sea sea) {
        EnumMap<SeaEvent, Integer> counts = new EnumMap<>(SeaEvent.class);
        for (Hex hex : sea.generatedHexes()) {
            SeaEvent event = sea.whatsAt(hex).pendingEvent();
            counts.put(event, counts.getOrDefault(event, 0) + 1);
        }
        return counts;
    }

    private static int distanceFromOrigin(Hex hex) {
        return Math.max(Math.abs(hex.q()), Math.max(Math.abs(hex.r()), Math.abs(hex.s())));
    }
}
