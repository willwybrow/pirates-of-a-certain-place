package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatMovementTest {

    @Test
    void playerMovesIntoDestinationImmediatelyWhenCombatEnds() throws Exception {
        Sea sea = new Sea();
        Hex start = sea.playerPosition();
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        forceTileAt(sea, destination, new MonsterTile(SeaEvent.KRAKEN, false, new Monster("Test Kraken", 1, 0, 0)));

        sea.attemptToTravel(direction);
        assertThat(sea.playerPosition()).isEqualTo(start);

        sea.attemptToAttack();

        assertThat(sea.playerPosition())
            .as("player should enter destination as soon as killing blow ends combat")
            .isEqualTo(destination);
    }

    @SuppressWarnings("unchecked")
    private static void forceTileAt(Sea sea, Hex hex, SeaTile forcedTile) throws Exception {
        Field generatedHexagonsField = Sea.class.getDeclaredField("generatedHexagons");
        generatedHexagonsField.setAccessible(true);
        Map<Hex, SeaTile> generatedHexagons = (Map<Hex, SeaTile>) generatedHexagonsField.get(sea);
        generatedHexagons.put(hex, forcedTile);
    }
}
