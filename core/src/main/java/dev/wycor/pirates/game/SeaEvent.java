package dev.wycor.pirates.game;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public enum SeaEvent {
    NOTHING(EmptyTile::generate),
    ISLAND(IslandTile::generate),
    KRAKEN(MonsterTile::generate),
    SQUID(MonsterTile::generate),
    GHOST(MonsterTile::generate);

    private final Supplier<? extends SeaTile> tileGenerator;

    SeaEvent(Supplier<? extends SeaTile> tileGenerator) {
        this.tileGenerator = tileGenerator;
    }

//    private final Function<PlayerDetails, PlayerDetails> eventResolver;
//
//    SeaEvent(Function<PlayerDetails, PlayerDetails> eventResolver) {
//        this.eventResolver = eventResolver;
//    }

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


    public SeaTile generate() {
        return this.tileGenerator.get();
    }
}
