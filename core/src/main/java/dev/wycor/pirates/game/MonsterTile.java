package dev.wycor.pirates.game;

public class MonsterTile extends SeaTile {
    private final Monster monsterHere = new Monster("Kraken", 10, 2, 1);
    private Combat fightWithPlayer;

    MonsterTile(SeaEvent seaEvent, boolean explored) {
        super(seaEvent, explored);
    }

    public static MonsterTile generate() {
        return new MonsterTile(SeaEvent.KRAKEN, false);
    }

    @Override
    public boolean isCompleted() {
        return this.monsterHere.isDead();
    }

    @Override
    protected Combat combatEvent(PlayerDetails playerDetails) {
        if (fightWithPlayer == null) {
            fightWithPlayer = new Combat(playerDetails, monsterHere);
        }
        return fightWithPlayer;
    }

    @Override
    protected PlayerDetails completionRewards(PlayerDetails playerDetails) {
        return playerDetails;
    }
}
