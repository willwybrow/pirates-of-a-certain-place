package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WorldGenerationTest {

    @Test
    void generatedCorrectlySizedWorld() {
        World world = new TileFactory(new Random(), new Random()).generate();

        assertEquals(169, world.generatedHexes().size());
    }
}
