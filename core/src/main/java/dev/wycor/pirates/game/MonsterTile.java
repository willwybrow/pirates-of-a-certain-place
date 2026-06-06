package dev.wycor.pirates.game;

import java.util.function.Supplier;

public class MonsterTile extends SeaTile {
    private final Supplier<Monster> monsterSupplier;
    private Monster monsterHere;
    private Combat fightWithPlayer;

    MonsterTile(SeaEvent seaEvent, boolean explored, Supplier<Monster> monsterSupplier) {
        super(seaEvent, explored);
        this.monsterSupplier = monsterSupplier;
        this.monsterHere = monsterSupplier.get();
    }

    public static MonsterTile kraken() {
        return new MonsterTile(SeaEvent.KRAKEN, false, () -> new Monster("Kraken", 10, 2, 1));
    }

    public static MonsterTile squid() {
        return new MonsterTile(SeaEvent.SQUID, false, () -> new Monster("Giant Squid", 8, 3, 1));
    }

    public static MonsterTile ghost() {
        return new MonsterTile(SeaEvent.GHOST, false, () -> new Monster("Ghost Ship", 9, 2, 2));
    }

    @Override
    public boolean isCompleted() {
        return this.monsterHere.isDead();
    }

    @Override
    protected Combat combatEvent(Player player) {
        if (fightWithPlayer == null) {
            fightWithPlayer = new Combat(player, monsterHere);
        }
        return fightWithPlayer;
    }

    @Override
    protected void completionRewards(Player player) {
    }

    @Override
    protected void handlePlayerFled() {
        this.monsterHere = monsterSupplier.get();
        this.fightWithPlayer = null;
    }
}
