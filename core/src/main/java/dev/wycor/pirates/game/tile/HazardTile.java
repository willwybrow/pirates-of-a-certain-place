package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;

/**
 * A hazard the player sails into. Like a treasure tile, encountering a hazard takes a moment to
 * resolve and then the hazard is spent: an {@link #ICEBERG} damages the ship, a {@link #WHIRLPOOL}
 * steals a captured treasure and re-hides it under an unexplored tile. The hazard effect itself is
 * applied by the game engine, which has access to the player and world; the tile only tracks which
 * hazard it is and whether it has already been triggered.
 */
public final class HazardTile extends SeaTile {

    public enum Hazard {
        ICEBERG,
        WHIRLPOOL
    }

    private final Hazard hazard;
    private boolean triggered;

    private HazardTile(SeaEvent seaEvent, Hazard hazard) {
        super(seaEvent, false);
        this.hazard = hazard;
    }

    public static HazardTile iceberg() {
        return new HazardTile(SeaEvent.ICEBERG, Hazard.ICEBERG);
    }

    public static HazardTile whirlpool() {
        return new HazardTile(SeaEvent.WHIRLPOOL, Hazard.WHIRLPOOL);
    }

    public Hazard hazard() {
        return this.hazard;
    }

    /** Marks the hazard as spent, after which it no longer occupies the tile. */
    public void trigger() {
        this.triggered = true;
    }

    @Override
    public boolean isCompleted() {
        return this.triggered;
    }

    @Override
    protected Monster combatant() {
        return null;
    }

    @Override
    protected Reward completionRewards() {
        return new Reward(0, 0);
    }
}
