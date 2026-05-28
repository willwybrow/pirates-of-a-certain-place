package dev.wycor.pirates;

public class SeaTile {
    private final SeaEvent seaEvent;
    private boolean explored = false;
    private boolean completed = false;

    private SeaTile(SeaEvent seaEvent, boolean explored) {
        this.seaEvent = seaEvent;
        this.explored = explored;
    }

    public boolean isExplored() {
        return explored;
    }

    public SeaEvent explore() {
        this.explored = true;
        return seaEvent;
    }

    public SeaEvent pendingEvent() {
        return completed ? SeaEvent.EMPTY : this.seaEvent;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void complete() {
        this.completed = true;
    }

    public static SeaTile startingSquare() {
        return new SeaTile(SeaEvent.EMPTY, true);
    }

    public static SeaTile generate() {
        return new SeaTile(SeaEvent.random(), false);
    }
}
