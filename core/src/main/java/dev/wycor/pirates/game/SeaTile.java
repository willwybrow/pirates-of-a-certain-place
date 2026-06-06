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

    public final boolean combatComplete(PlayerDetails playerDetails) {
        return Optional.ofNullable(this.getCombatEvent(playerDetails)).map(Combat::isOver).orElse(true);
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

    public final PlayerDetails applyRewards(PlayerDetails playerDetails) {
        PlayerDetails rewardedPlayer = this.completionRewards(playerDetails);
        this.rewarded = true;
        return rewardedPlayer;
    }

    public final Combat getCombatEvent(PlayerDetails playerDetails) {
        return this.combatEvent(playerDetails);
    }

    protected abstract Combat combatEvent(PlayerDetails playerDetails);

    protected abstract PlayerDetails completionRewards(PlayerDetails playerDetails);
}
