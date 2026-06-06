package dev.wycor.pirates.game;

import java.util.Optional;

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

    public static SeaTile random() {
        return SeaEvent.random().generate();
    }

    public boolean isSpied() {
        return spied;
    }

    public SeaTile spy() {
        this.spied = true;
        return this;
    }

    public final boolean combatComplete(Player player) {
        return Optional.ofNullable(this.getCombatEvent(player)).map(Combat::isOver).orElse(true);
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

    public final Combat getCombatEvent(Player player) {
        return this.combatEvent(player);
    }

    public final void onPlayerFled() {
        this.handlePlayerFled();
    }

    protected abstract Combat combatEvent(Player player);

    protected abstract Reward completionRewards();

    protected void handlePlayerFled() {
    }
}
