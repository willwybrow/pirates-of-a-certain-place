package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.assertj.core.api.Assertions.assertThat;

class TreasureGenerationTest {

    @Test
    void generatedWorldHasOriginalFixedComposition() {
        GameRandom gameRandom = new GameRandom(0L);
        TileFactory tileFactory = new TileFactory(gameRandom.world(), gameRandom.monster());
        AttackResolver attackResolver = new AttackResolver(gameRandom.monster());
        HazardEngine hazardEngine = new HazardEngine(gameRandom.hazard());
        ItemEngine itemEngine = new ItemEngine();
        Game game = new Game(tileFactory, attackResolver, hazardEngine, itemEngine);
        game.startNewGame(42L);

        EnumMap<SeaEvent, Integer> counts = countPendingEvents(game);

        assertThat(game.generatedHexes()).hasSize(169);
        assertThat(counts.getOrDefault(SeaEvent.ISLAND, 0)).isEqualTo(8);
        assertThat(counts.getOrDefault(SeaEvent.GIANT_SQUID, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.SEAWEED_MONSTER, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.PHOENIX, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.GHOST_SHIP, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.PIRATE_SHIP, 0)).isEqualTo(6);
        assertThat(counts.getOrDefault(SeaEvent.WHIRLPOOL, 0)).isEqualTo(2);
        assertThat(counts.getOrDefault(SeaEvent.ICEBERG, 0)).isEqualTo(2);
        assertThat(counts.getOrDefault(SeaEvent.TAR, 0)).isEqualTo(3);
        assertThat(counts.getOrDefault(SeaEvent.MAP, 0)).isEqualTo(3);
        assertThat(counts.getOrDefault(SeaEvent.SEXTANT, 0)).isEqualTo(4);
        assertThat(counts.getOrDefault(SeaEvent.SPYGLASS, 0)).isEqualTo(4);

        for (Treasure treasure : Treasure.values()) {
            assertThat(counts.getOrDefault(SeaEvent.valueOf(treasure.name()), 0))
                .as("treasure %s should appear exactly once", treasure)
                .isEqualTo(1);
        }

        assertThat(counts.getOrDefault(SeaEvent.NOTHING, 0)).isEqualTo(103);
    }

    private static EnumMap<SeaEvent, Integer> countPendingEvents(Game game) {
        EnumMap<SeaEvent, Integer> counts = new EnumMap<>(SeaEvent.class);
        for (Hex hex : game.generatedHexes()) {
            SeaEvent event = game.whatsAt(hex).pendingEvent();
            counts.put(event, counts.getOrDefault(event, 0) + 1);
        }
        return counts;
    }
}
