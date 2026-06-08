package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;

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
    protected Monster combatant() {
        return null;
    }

    @Override
    protected Reward completionRewards() {
        if (suppliesAvailable) {
            suppliesAvailable = false;
            return new Reward(10, FOOD_AVAILABLE, 1);
        }

        return new Reward(0, 0);
    }

}
