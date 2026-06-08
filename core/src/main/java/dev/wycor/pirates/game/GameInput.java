package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;

import java.util.Objects;

/**
 * An immutable record of a single player decision, plus the timestamp at which it was made.
 *
 * <p>A game is fully reproducible from its seed and the ordered list of {@code GameInput}s: replaying
 * the same inputs in the same order against the same seed reconstructs the exact same game state.
 * Inputs capture only what the player chose; any derived data (such as a travel destination) is
 * computed during simulation from the state at the time the input is processed.
 */
public abstract class GameInput {

    private final long timestampMillis;

    private GameInput(long timestampMillis) {
        this.timestampMillis = timestampMillis;
    }

    public long timestampMillis() {
        return this.timestampMillis;
    }

    public static Travel travel(Direction direction, long timestampMillis) {
        return new Travel(Objects.requireNonNull(direction, "direction"), timestampMillis);
    }

    public static Attack attack(Weapon weapon, long timestampMillis) {
        return new Attack(Objects.requireNonNull(weapon, "weapon"), timestampMillis);
    }

    public static Flee flee(long timestampMillis) {
        return new Flee(timestampMillis);
    }

    public static final class Travel extends GameInput {
        private final Direction direction;

        private Travel(Direction direction, long timestampMillis) {
            super(timestampMillis);
            this.direction = direction;
        }

        public Direction direction() {
            return this.direction;
        }
    }

    public static final class Attack extends GameInput {
        private final Weapon weapon;

        private Attack(Weapon weapon, long timestampMillis) {
            super(timestampMillis);
            this.weapon = weapon;
        }

        public Weapon weapon() {
            return this.weapon;
        }
    }

    public static final class Flee extends GameInput {
        private Flee(long timestampMillis) {
            super(timestampMillis);
        }
    }
}
