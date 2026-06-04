package dev.wycor.pirates.game;

import java.util.Random;

public enum SeaEvent {
    NOTHING,
    ISLAND,
    KRAKEN,
    SQUID,
    GHOST;

    public static SeaEvent random() {
        int random = new Random().nextInt(100);
        if (random < 10) {
            return ISLAND;
        }
        if (random < 15) {
            return SQUID;
        }
        if (random < 20) {
            return KRAKEN;
        }
        if (random < 25) {
            return GHOST;
        }
        return NOTHING;
    }
}
