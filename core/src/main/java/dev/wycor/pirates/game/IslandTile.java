package dev.wycor.pirates.game;

public class IslandTile extends SeaTile {
    private int suppliesAvailable = 20;

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

    }
