package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Hazard;
import dev.wycor.pirates.game.Item;
import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;

public final class ItemTile extends SeaTile {
    private static final int TAR_HEALTH_RESTORE = 15;

    private final Item item;
    private boolean claimed;

    private ItemTile(SeaEvent seaEvent, Item item) {
        super(seaEvent, false);
        this.item = item;
    }

    public static ItemTile tar() {
        return new ItemTile(SeaEvent.TAR, Item.TAR);
    }

    public static ItemTile map() {
        return new ItemTile(SeaEvent.MAP, Item.MAP);
    }

    public static ItemTile sextant() {
        return new ItemTile(SeaEvent.SEXTANT, Item.SEXTANT);
    }

    public static ItemTile spyglass() {
        return new ItemTile(SeaEvent.SPYGLASS, Item.SPYGLASS);
    }

    @Override
    protected Item item() {
        return this.item;
    }

    @Override
    protected Hazard hazard() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return this.claimed;
    }

    @Override
    protected void complete() {
        this.claimed = true;
    }

    @Override
    protected Monster combatant() {
        return null;
    }

    @Override
    protected Reward reward() {
        if (this.claimed) {
            return null;
        }

        if (this.item == Item.TAR) {
            return new Reward(TAR_HEALTH_RESTORE, 0);
        }
        return null;
    }
}
