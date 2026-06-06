package dev.wycor.pirates.game;

import java.util.Random;
import java.util.function.Supplier;

public enum SeaEvent {
    NOTHING(EmptyTile::generate),
    ISLAND(IslandTile::generate),
    GIANT_SQUID(MonsterTile::giantSquid),
    SEAWEED_MONSTER(MonsterTile::seaweedMonster),
    PHOENIX(MonsterTile::phoenix),
    GHOST_SHIP(MonsterTile::ghostShip),
    PIRATE_SHIP(MonsterTile::pirateShip);

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
            return GIANT_SQUID;
        }
        if (random < 20) {
            return SEAWEED_MONSTER;
        }
        if (random < 25) {
            return PHOENIX;
        }
        if (random < 30) {
            return GHOST_SHIP;
        }
        if (random < 35) {
            return PIRATE_SHIP;
        }
        return NOTHING;
    }


    public SeaTile generate() {
        return this.tileGenerator.get();
    }
}
