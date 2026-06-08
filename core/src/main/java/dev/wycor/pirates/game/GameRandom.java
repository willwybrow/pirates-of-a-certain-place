package dev.wycor.pirates.game;

import java.util.Random;

/**
 * Owns all randomness for a single game, derived deterministically from the game seed.
 *
 * <p>The seed is expanded into independent substreams (one per purpose) so that drawing
 * from one stream never shifts the values produced by another. This keeps, for example,
 * world generation reproducible even if the number of combat rolls in a game changes.
 */
public final class GameRandom {

    private final Random worldGen;
    private final Random combat;
    private final Random hazard;

    public GameRandom(long seed) {
        // Expand the single game seed into independent sub-seeds. Using a dedicated
        // seeding Random keeps the derivation deterministic and stable across runs.
        Random seeding = new Random(seed);
        this.worldGen = new Random(seeding.nextLong());
        this.combat = new Random(seeding.nextLong());
        this.hazard = new Random(seeding.nextLong());
    }

    /** Random stream used while generating the world layout from the seed. */
    public Random worldGen() {
        return this.worldGen;
    }

    /** Random stream used while resolving combat (damage variance, etc.). */
    public Random combat() {
        return this.combat;
    }

    /** Random stream used while resolving hazards (which treasure/tile a whirlpool affects, etc.). */
    public Random hazard() {
        return this.hazard;
    }
}
