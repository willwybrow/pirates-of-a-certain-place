package dev.wycor.pirates.game;

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
