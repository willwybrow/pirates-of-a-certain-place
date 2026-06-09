package dev.wycor.pirates.game;

import java.util.Random;

abstract class TestGameFactory extends TileFactory {

    private final GameRandom gameRandom;

    TestGameFactory() {
        this(newDeterministicGameRandom());
    }

    private TestGameFactory(GameRandom gameRandom) {
        super(gameRandom);
        this.gameRandom = gameRandom;
    }

    final Game createGame() {
        return new Game(this.gameRandom, this).startNewGame(0L);
    }

    static Game createDefaultGame() {
        GameRandom gameRandom = newDeterministicGameRandom();
        return new Game(gameRandom, new TileFactory(gameRandom)).startNewGame(0L);
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
