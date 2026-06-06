package dev.wycor.pirates.game;

public class TreasureTile extends SeaTile {
    private final Treasure treasure;
    private boolean claimed;

    TreasureTile(SeaEvent seaEvent, Treasure treasure) {
        super(seaEvent, false);
        this.treasure = treasure;
    }

    public static TreasureTile emeraldOfHope() {
        return new TreasureTile(SeaEvent.EMERALD_OF_HOPE, Treasure.EMERALD_OF_HOPE);
    }

    public static TreasureTile goldenSwordOfYr() {
        return new TreasureTile(SeaEvent.GOLDEN_SWORD_OF_YR, Treasure.GOLDEN_SWORD_OF_YR);
    }

    public static TreasureTile kingFlynnsRoyalSceptre() {
        return new TreasureTile(SeaEvent.KING_FLYNNS_ROYAL_SCEPTRE, Treasure.KING_FLYNNS_ROYAL_SCEPTRE);
    }

    public static TreasureTile sacredOnyxCross() {
        return new TreasureTile(SeaEvent.SACRED_ONYX_CROSS, Treasure.SACRED_ONYX_CROSS);
    }

    public static TreasureTile lostPearlOfJehva() {
        return new TreasureTile(SeaEvent.LOST_PEARL_OF_JEHVA, Treasure.LOST_PEARL_OF_JEHVA);
    }

    public static TreasureTile queenLathasCrown() {
        return new TreasureTile(SeaEvent.QUEEN_LATHAS_CROWN, Treasure.QUEEN_LATHAS_CROWN);
    }

    public static TreasureTile rubyRingOfPower() {
        return new TreasureTile(SeaEvent.RUBY_RING_OF_POWER, Treasure.RUBY_RING_OF_POWER);
    }

    public static TreasureTile silverChaliceOfAunge() {
        return new TreasureTile(SeaEvent.SILVER_CHALICE_OF_AUNGE, Treasure.SILVER_CHALICE_OF_AUNGE);
    }

    public static TreasureTile murphysChestOfGold() {
        return new TreasureTile(SeaEvent.MURPHYS_CHEST_OF_GOLD, Treasure.MURPHYS_CHEST_OF_GOLD);
    }

    public static TreasureTile queenLathasNecklace() {
        return new TreasureTile(SeaEvent.QUEEN_LATHAS_NECKLACE, Treasure.QUEEN_LATHAS_NECKLACE);
    }

    @Override
    public boolean isCompleted() {
        return this.claimed;
    }

    @Override
    protected Combatant combatant() {
        return null;
    }

    @Override
    protected Reward completionRewards() {
        this.claimed = true;
        return new Reward(this.treasure);
    }
}
