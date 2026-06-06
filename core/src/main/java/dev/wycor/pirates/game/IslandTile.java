package dev.wycor.pirates.game;

public class IslandTile extends SeaTile {
    private static final int FOOD_AVAILABLE = 10;
    private static final int HEALTH_AVAILABLE = 10;

    private boolean suppliesAvailable = true;

    IslandTile() {
        super(SeaEvent.ISLAND, true); // always show islands
    }

    public static IslandTile generate() {
        return new IslandTile();
    }

    @Override
    public boolean isCompleted() {
        return !this.suppliesAvailable;
    }

    @Override
    protected Combat combatEvent(Player player) {
        return null;
    }

    @Override
    protected void completionRewards(Player player) {
        if (suppliesAvailable) {
            player.addFood(FOOD_AVAILABLE);
            player.heal(HEALTH_AVAILABLE);
            suppliesAvailable = false;
        }
    }

}
