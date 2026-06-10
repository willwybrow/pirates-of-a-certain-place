package dev.wycor.pirates.game;

import java.util.Random;

abstract class TestGameFactory extends TileFactory {

    private final AttackResolver attackResolver;
    private final HazardEngine hazardEngine;
    private final ItemEngine itemEngine;

    TestGameFactory() {
        this(newDeterministicGameRandom());
    }

    private TestGameFactory(GameRandom gameRandom) {
        super(gameRandom.world(), gameRandom.monster());
        this.attackResolver = new AttackResolver(gameRandom.monster());
        this.hazardEngine = new HazardEngine(gameRandom.hazard());
        this.itemEngine = new ItemEngine();
    }

    final Game createGame() {
        return new Game(this, this.attackResolver, this.hazardEngine, this.itemEngine).startNewGame(0L);
    }

    static Game createDefaultGame() {
        GameRandom gameRandom = newDeterministicGameRandom();
        TileFactory tileFactory = new TileFactory(gameRandom.world(), gameRandom.monster());
        AttackResolver attackResolver = new AttackResolver(gameRandom.monster());
        HazardEngine hazardEngine = new HazardEngine(gameRandom.hazard());
        ItemEngine itemEngine = new ItemEngine();
        return new Game(tileFactory, attackResolver, hazardEngine, itemEngine).startNewGame(0L);
    }

    private static GameRandom newDeterministicGameRandom() {
        Random seedSource = new Random() {
            @Override
            public long nextLong() {
                return 0L;
            }
        };
        return new GameRandom(seedSource.nextLong());
    }
}
