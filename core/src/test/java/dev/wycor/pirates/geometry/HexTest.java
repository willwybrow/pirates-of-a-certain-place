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
        Hex arbitraryHex = new Hex(113, 224);

        // then
        assertThat(arbitraryHex.neighbours()).containsExactly(
            new Hex(114, 223),
            new Hex(114, 224),
            new Hex(113, 225),
            new Hex(112, 225),
            new Hex(112, 224),
            new Hex(113, 223)
        );
    }

    @Test
    void neighbourDistance() {
        Hex arbitraryHex = arbitraryHex();

        assertThat(arbitraryHex.neighbours())
            .map(arbitraryHex::distanceTo)
            .containsExactly(1, 1, 1, 1, 1, 1);
    }

    @Test
    void testEquals() {
        var q = new Random().nextInt();
        var r = new Random().nextInt();

        var instanceOne = new Hex(q, r);
        var instanceTwo = new Hex(q, r);

        assertThat(instanceOne).isEqualTo(instanceTwo);
    }

    @Test
    void sCoordinateMatchesCubeInvariant() {
        Hex hex = new Hex(7, -3);

        assertThat(hex.s()).isEqualTo(-4);
        assertThat(hex.q() + hex.r() + hex.s()).isZero();
    }

    @Test
    void neighbourMatchesDirectionMove() {
        Hex hex = new Hex(-4, 11);

        for (Direction direction : Direction.values()) {
            assertThat(hex.neighbour(direction)).isEqualTo(direction.move(hex));
        }
    }

    @Test
    void neighboursAreCachedPerHex() {
        Hex hex = new Hex(2, 3);

        assertThat(hex.neighbours()).isSameAs(hex.neighbours());
    }

    @Test
    void equalsReturnsFalseForNullOrDifferentType() {
        Hex hex = new Hex(0, 0);

        assertThat(hex.equals(null)).isFalse();
        assertThat(hex.equals("not a hex")).isFalse();
    }

    @Test
    void equalHexesHaveEqualHashCodes() {
        Hex one = new Hex(9, -4);
        Hex two = new Hex(9, -4);

        assertThat(one.hashCode()).isEqualTo(two.hashCode());
    }

    private static Hex arbitraryHex() {
        Random random = new Random();
        return new Hex(random.nextInt(), random.nextInt());
    }
}
