package dev.wycor.pirates.geometry;

public enum Direction {
    NORTHEAST(+1, -1),
    EAST(+1, 0),
    SOUTHEAST(0, +1),
    SOUTHWEST(-1, +1),
    WEST(-1, 0),
    NORTHWEST(0, -1);

    private final int deltaQ;
    private final int deltaR;

    Direction(int deltaQ, int deltaR) {
        this.deltaQ = deltaQ;
        this.deltaR = deltaR;
    }

    public Hex move(Hex from) {
        return new Hex(from.q() + this.deltaQ, from.r() + this.deltaR);
    }
}
