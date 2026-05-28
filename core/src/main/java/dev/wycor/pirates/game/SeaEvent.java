package dev.wycor.pirates.game;

import java.util.Random;

public enum SeaEvent {
    EMPTY,
    ISLAND,
    KRAKEN;

    public static SeaEvent random() {
        int random = new Random().nextInt();
        if (random % 4 == 0) {
            return KRAKEN;
        }
        if (random % 3 == 0) {
            return ISLAND;
        }
        return EMPTY;
    }
}
