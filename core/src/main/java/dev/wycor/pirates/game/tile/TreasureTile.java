package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Hazard;
import dev.wycor.pirates.game.Item;
import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.Treasure;

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

    public static TreasureTile forTreasure(Treasure treasure) {
        switch (treasure) {
            case EMERALD_OF_HOPE:
                return emeraldOfHope();
            case GOLDEN_SWORD_OF_YR:
                return goldenSwordOfYr();
            case KING_FLYNNS_ROYAL_SCEPTRE:
                return kingFlynnsRoyalSceptre();
            case SACRED_ONYX_CROSS:
                return sacredOnyxCross();
            case LOST_PEARL_OF_JEHVA:
                return lostPearlOfJehva();
            case QUEEN_LATHAS_CROWN:
                return queenLathasCrown();
            case RUBY_RING_OF_POWER:
                return rubyRingOfPower();
            case SILVER_CHALICE_OF_AUNGE:
                return silverChaliceOfAunge();
            case MURPHYS_CHEST_OF_GOLD:
                return murphysChestOfGold();
            case QUEEN_LATHAS_NECKLACE:
                return queenLathasNecklace();
            default:
                throw new IllegalStateException("Unexpected treasure: " + treasure);
        }
    }

    @Override
    public boolean isCompleted() {
        return this.claimed;
    }

    @Override
    protected void complete() {
        this.claimed = true;
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
    protected Monster combatant() {
        return null;
    }

    @Override
    protected Reward reward() {
        if (this.claimed) {
            return null;
        }
        return new Reward(this.treasure);
    }
}
