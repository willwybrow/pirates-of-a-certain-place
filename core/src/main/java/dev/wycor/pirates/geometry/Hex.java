package dev.wycor.pirates.geometry;

import java.util.List;
import java.util.Objects;

public class Hex {
    public static final Hex ORIGIN = new Hex(0, 0);
    private final int q;
    private final int r;

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

    public List<Hex> neighbours() {
        return List.of(
            new Hex(this.q + 1, this. r - 1),
            new Hex(this.q + 1, this.r),
            new Hex(this.q, this.r + 1),
            new Hex(this.q - 1, this.r + 1),
            new Hex(this.q - 1, this.r),
            new Hex(this.q, this.r - 1)
        );
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
