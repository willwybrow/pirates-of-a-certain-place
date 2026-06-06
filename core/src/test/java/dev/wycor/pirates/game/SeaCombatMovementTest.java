package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SeaCombatMovementTest {

    @Test
    void playerMovesIntoDestinationImmediatelyWhenCombatEnds() {
        Hex start = Hex.ORIGIN;
        Direction direction = Direction.EAST;
        Hex destination = direction.move(start);

        Sea sea = new Sea(new TileFactory() {
            @Override
            public SeaTile create(Hex hex) {
                if (destination.equals(hex)) {
                    return new MonsterTile(SeaEvent.KRAKEN, false, () -> new Monster("Test Kraken", 1, 0, 0));
                }
                return super.create(hex);
            }
        });

        sea.attemptToTravel(direction);
        assertThat(sea.playerPosition()).isEqualTo(start);

        sea.attemptToAttack();

        assertThat(sea.playerPosition())
            .as("player should enter destination as soon as killing blow ends combat")
            .isEqualTo(destination);
    }
}
