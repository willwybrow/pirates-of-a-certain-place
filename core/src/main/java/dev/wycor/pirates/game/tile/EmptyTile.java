package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Hazard;
import dev.wycor.pirates.game.Item;
import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;

public class EmptyTile extends SeaTile {

    EmptyTile() {
        super(SeaEvent.NOTHING, false);
    }

    public static EmptyTile generate() {
        return new EmptyTile();
    }

    @Override
    protected Item item() {
        return null;
    }

    @Override
    protected Hazard hazard() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return true;
    }

    @Override
    protected void complete() {

    }

    @Override
    protected Monster combatant() {
        return null;
    }

    @Override
    protected Reward reward() {
        return null;
    }
}
