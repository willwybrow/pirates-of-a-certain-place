package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Hex;

public class Travel {
    private Hex from;
    private Hex to;

    public Travel(Hex from, Hex to) {
        this.from = from;
        this.to = to;
    }

    public Hex headingFrom() {
        return from;
    }

    public Hex headingTo() {
        return to;
    }
}
