package dev.wycor.pirates.game.tile;

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
    public boolean isCompleted() {
        return true;
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
