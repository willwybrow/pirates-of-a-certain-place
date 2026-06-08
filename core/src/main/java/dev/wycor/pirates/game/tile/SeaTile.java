package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.Treasure;

import java.util.EnumMap;
import java.util.Random;

public abstract class SeaTile {
    private final SeaEvent seaEvent;
    private boolean spied = false;
    private boolean rewarded;

    protected SeaTile(SeaEvent seaEvent, boolean spied) {
        this.seaEvent = seaEvent;
        this.spied = spied;
    }

    public static SeaTile startingSquare() {
        return EmptyTile.generate().spy();
    }

    public static SeaTile random(EnumMap<Treasure, Boolean> unavailableTreasures, Random worldGenRandom) {
        return SeaEvent.random(unavailableTreasures, worldGenRandom).generate();
    }

    public boolean isSpied() {
        return spied;
    }

    public SeaTile spy() {
        this.spied = true;
        return this;
    }

    public SeaEvent pendingEvent() {
        return this.isCompleted() ? SeaEvent.NOTHING : this.seaEvent;
    }

    public SeaEvent completedEvent() {
        return this.isCompleted() ? this.seaEvent : SeaEvent.NOTHING;
    }

    public abstract boolean isCompleted();

    public final boolean isPlayerRewarded() {
        return rewarded;
    }

    public final Reward applyRewards() {
        Reward reward = this.completionRewards();
        this.rewarded = true;
        return reward;
    }

    public final Monster getCombatant() {
        return this.combatant();
    }

    public final void onPlayerFled() {
        this.handlePlayerFled();
    }

    protected abstract Monster combatant();

    protected abstract Reward completionRewards();

    protected void handlePlayerFled() {
    }
}
