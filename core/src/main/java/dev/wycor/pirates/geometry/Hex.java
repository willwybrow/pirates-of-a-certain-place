package dev.wycor.pirates.geometry;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Hex {
    public static final Hex ORIGIN = new Hex(0, 0);

    private final int q;
    private final int r;

    private LinkedHashSet<Hex> neighbours = null;

    public Hex(int q, int r) {
        this.q = q;
        this.r = r;
    }

    public int q() {
        return this.q;
    }

    public int r() {
        return this.r;
    }

    public int s() {
        return - this.q - this.r;
    }

    public LinkedHashSet<Hex> neighbours() {
        if (neighbours == null) {
            neighbours = new LinkedHashSet<>(List.of(
                new Hex(this.q + 1, this. r - 1),
                new Hex(this.q + 1, this.r),
                new Hex(this.q, this.r + 1),
                new Hex(this.q - 1, this.r + 1),
                new Hex(this.q - 1, this.r),
                new Hex(this.q, this.r - 1)
            ));
        }
        return neighbours;
    }

    public Stream<Hex> spiral(int layers) {
        HashSet<Hex> hexes = new HashSet<>();
        var n = Math.abs(layers);

        for (int q = -n; q<= n; q++) {
            for (int r = Math.max(-n, -q-n); r <= Math.min(n, -q+n); r++) {
                hexes.add(new Hex(this.q() + q, this.r() + r));
            }
        }

        return hexes.stream();
    }

    public Hex neighbour(Direction direction) {
        return direction.move(this);
    }

    /**
     *
     * @param n
     * @return the nth centred hexagonal number
     */
    public static int centredHexagonalNumber(int n) {
        return (int)Math.round(Math.pow(n, 3) - Math.pow(n-1, 3));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Hex hex = (Hex) o;
        return q == hex.q && r == hex.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }
}
