package dev.wycor.pirates.geometry;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class HexTest {

    @Test
    void neighbours() {
        // given
        Hex origin = new Hex(0, 0);

        // then
        assertThat(origin.neighbours()).containsExactly(
            new Hex(1, -1),
            new Hex(1, 0),
            new Hex(0, 1),
            new Hex(-1, 1),
            new Hex(-1, 0),
            new Hex(0, -1)
        );
    }

    @Test
    void bigNeighbours() {
        // given
        Hex origin = new Hex(113, 224);

        // then
        assertThat(origin.neighbours()).containsExactly(
            new Hex(114, 223),
            new Hex(114, 224),
            new Hex(113, 225),
            new Hex(112, 225),
            new Hex(112, 224),
            new Hex(113, 223)
        );
    }

    @Test
    void testEquals() {
        var q = new Random().nextInt();
        var r = new Random().nextInt();

        var instanceOne = new Hex(q, r);
        var instanceTwo = new Hex(q, r);

        assertThat(instanceOne).isEqualTo(instanceTwo);
    }
}
