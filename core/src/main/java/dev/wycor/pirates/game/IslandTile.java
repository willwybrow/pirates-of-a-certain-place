package dev.wycor.pirates.game;

public class IslandTile extends SeaTile {
    private int suppliesAvailable = 10;

    IslandTile() {
        super(SeaEvent.ISLAND, true); // always show islands
    }

    public static IslandTile generate() {
        return new IslandTile();
    }

    @Override
    public boolean isCompleted() {
        return this.suppliesAvailable == 0;
    }

    @Override
    protected Combat combatEvent(PlayerDetails playerDetails) {
        return null;
    }

    @Override
    protected PlayerDetails completionRewards(PlayerDetails playerDetails) {
        if (suppliesAvailable > 0) {
            playerDetails.addFood(suppliesAvailable);
            suppliesAvailable = 0;
        }
        return playerDetails;
    }

}
