package dev.wycor.pirates.game;

public class MonsterTile extends SeaTile {
    private final Monster monsterHere;
    private Combat fightWithPlayer;

    MonsterTile(SeaEvent seaEvent, boolean explored, Monster monsterHere) {
        super(seaEvent, explored);
        this.monsterHere = monsterHere;
    }

    public static MonsterTile kraken() {
        return new MonsterTile(SeaEvent.KRAKEN, false, new Monster("Kraken", 10, 2, 1));
    }

    public static MonsterTile squid() {
        return new MonsterTile(SeaEvent.SQUID, false, new Monster("Giant Squid", 8, 3, 1));
    }

    public static MonsterTile ghost() {
        return new MonsterTile(SeaEvent.GHOST, false, new Monster("Ghost Ship", 9, 2, 2));
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
        playerDetails.heal(2);
        return playerDetails;
    }
}
