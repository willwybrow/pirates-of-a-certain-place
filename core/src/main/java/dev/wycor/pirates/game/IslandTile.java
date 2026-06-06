package dev.wycor.pirates.game;

public class IslandTile extends SeaTile {
    private static final int FOOD_AVAILABLE = 10;

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
    protected Combatant combatant() {
        return null;
    }

    @Override
    protected Reward completionRewards() {
        if (suppliesAvailable) {
            suppliesAvailable = false;
            return Reward.food(FOOD_AVAILABLE);
        }

        return Reward.none();
    }

}
