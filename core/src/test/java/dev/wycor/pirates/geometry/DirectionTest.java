package dev.wycor.pirates.geometry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DirectionTest {

    @Test
    void moveUsesExpectedCoordinateDeltasForEveryDirection() {
        Hex from = new Hex(5, -2);

        assertThat(Direction.NORTHEAST.move(from)).isEqualTo(new Hex(6, -3));
        assertThat(Direction.EAST.move(from)).isEqualTo(new Hex(6, -2));
        assertThat(Direction.SOUTHEAST.move(from)).isEqualTo(new Hex(5, -1));
        assertThat(Direction.SOUTHWEST.move(from)).isEqualTo(new Hex(4, -1));
        assertThat(Direction.WEST.move(from)).isEqualTo(new Hex(4, -2));
        assertThat(Direction.NORTHWEST.move(from)).isEqualTo(new Hex(5, -3));

        assertThat(from).isEqualTo(new Hex(5, -2));
    }
}
