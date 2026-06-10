package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Hazard;
import dev.wycor.pirates.game.Item;
import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.Treasure;

import java.util.EnumMap;
import java.util.Random;

public abstract class SeaTile {
    private final SeaEvent seaEvent;
    private boolean spied = false;

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

    public final boolean hasReward() {
        return this.reward() != null;
    }

    public final Reward applyRewards() {
        return this.completeForReward();
    }

    public final Monster getCombatant() {
        return this.combatant();
    }

    public final Item getItem() {
        return this.item();
    }

    public final Hazard getHazard() {
        return this.hazard();
    }

    public final Reward getReward() {
        return this.reward();
    }

    public final void onPlayerFled() {
        this.handlePlayerFled();
    }

    public final Reward completeForReward() {
        if (this.isCompleted()) {
            return null;
        }
        Reward reward = this.reward();
        this.complete();
        return reward;
    }

    public abstract boolean isCompleted();

    protected abstract void complete();

    protected abstract Item item();

    protected abstract Hazard hazard();

    protected abstract Monster combatant();

    protected abstract Reward reward();

    protected void handlePlayerFled() {
    }
}
