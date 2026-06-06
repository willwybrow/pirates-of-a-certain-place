package dev.wycor.pirates.game;

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

    public final Combatant getCombatant() {
        return this.combatant();
    }

    public final void onPlayerFled() {
        this.handlePlayerFled();
    }

    protected abstract Combatant combatant();

    protected abstract Reward completionRewards();

    protected void handlePlayerFled() {
    }
}
