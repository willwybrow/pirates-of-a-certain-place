package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Hazard;
import dev.wycor.pirates.game.Item;
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

    @Override
    protected Item item() {
        return null;
    }

    @Override
    protected Hazard hazard() {
        return this.hazard;
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
    protected void complete() {
        this.triggered = true;
    }

    @Override
    protected Reward reward() {
        return null;
    }
}
