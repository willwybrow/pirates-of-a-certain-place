package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.monster.*;

import java.util.function.Supplier;

public class MonsterTile extends SeaTile {
    private final Supplier<Monster> monsterSpawner;
    private Monster monsterHere;

    MonsterTile(SeaEvent seaEvent, boolean explored, Supplier<Monster> monsterSpawner) {
        super(seaEvent, explored);
        this.monsterSpawner = monsterSpawner;
        this.monsterHere = monsterSpawner.get();
    }

    public static MonsterTile giantSquid() {
        return new MonsterTile(SeaEvent.GIANT_SQUID, false, GiantSquid::new);
    }

    public static MonsterTile seaweedMonster() {
        return new MonsterTile(SeaEvent.SEAWEED_MONSTER, false, SeaweedMonster::new);
    }

    public static MonsterTile phoenix() {
        return new MonsterTile(SeaEvent.PHOENIX, false, Phoenix::new);
    }

    public static MonsterTile ghostShip() {
        return new MonsterTile(SeaEvent.GHOST_SHIP, false, GhostShip::new);
    }

    public static MonsterTile pirateShip() {
        return new MonsterTile(SeaEvent.PIRATE_SHIP, false, PirateShip::new);
    }

    @Override
    public boolean isCompleted() {
        return this.monsterHere.isDead();
    }

    @Override
    protected Monster combatant() {
        return this.monsterHere;
    }

    @Override
    protected Reward completionRewards() {
        return new Reward(0, 0);
    }

    @Override
    protected void handlePlayerFled() {
        this.monsterHere = monsterSpawner.get();
    }
}
