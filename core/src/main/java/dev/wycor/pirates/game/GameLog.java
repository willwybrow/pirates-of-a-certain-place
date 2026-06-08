package dev.wycor.pirates.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The authoritative, append-only record of a single game: its seed plus the ordered list of player
 * inputs. The entire game is reproducible from this log alone, so it is the unit that would be
 * serialized for save/load (left in-memory for now).
 */
public final class GameLog {

    private final long seed;
    private final List<GameInput> inputs = new ArrayList<>();

    public GameLog(long seed) {
        this.seed = seed;
    }

    public long seed() {
        return this.seed;
    }

    public void append(GameInput input) {
        this.inputs.add(input);
    }

    public List<GameInput> inputs() {
        return Collections.unmodifiableList(this.inputs);
    }
}
