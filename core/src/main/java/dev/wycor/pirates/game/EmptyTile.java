package dev.wycor.pirates.game;

public class EmptyTile extends SeaTile {

    private boolean visited;

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
    protected Combat combatEvent(PlayerDetails playerDetails) {
        return null;
    }

    @Override
    protected PlayerDetails completionRewards(PlayerDetails playerDetails) {
        return playerDetails;
    }
}
