package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class WorldBalanceTest {

    @Test
    void thereIsNeverEnoughFoodToEat() {
        World world = new TileFactory(new Random(), new Random()).generate();

        int totalFoodAvailable = world.generatedHexes().stream().map(world::whatsAt).map(SeaTile::getReward).filter(Objects::nonNull).map(Reward::food).mapToInt(Integer::intValue).sum();
        assertThat(totalFoodAvailable).isLessThan(world.generatedHexes().size()); // more food than tiles lets you always flee every monster encounter for free
    }
}
