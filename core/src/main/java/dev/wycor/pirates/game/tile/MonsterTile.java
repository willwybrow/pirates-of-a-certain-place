package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Hazard;
import dev.wycor.pirates.game.Item;
import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.Reward;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.monster.*;

import java.util.Random;
import java.util.function.Supplier;

public class MonsterTile extends SeaTile {
    private final Supplier<Monster> monsterSpawner;
    private boolean lootClaimed;
    private Monster monsterHere;

    MonsterTile(SeaEvent seaEvent, boolean explored, Supplier<Monster> monsterSpawner) {
        super(seaEvent, explored);
        this.monsterSpawner = monsterSpawner;
        this.monsterHere = monsterSpawner.get();
        this.lootClaimed = false;
    }

    public static MonsterTile giantSquid() {
        return new MonsterTile(SeaEvent.GIANT_SQUID, false, GiantSquid::new);
    }

    public static MonsterTile giantSquid(Random random) {
        int health = GiantSquid.rollHealth(random);
        return new MonsterTile(SeaEvent.GIANT_SQUID, false, () -> GiantSquid.withHealth(health));
    }

    public static MonsterTile seaweedMonster() {
        return new MonsterTile(SeaEvent.SEAWEED_MONSTER, false, SeaweedMonster::new);
    }

    public static MonsterTile phoenix() {
        return new MonsterTile(SeaEvent.PHOENIX, false, Phoenix::new);
    }

    public static MonsterTile phoenix(Random random) {
        int health = Phoenix.rollHealth(random);
        return new MonsterTile(SeaEvent.PHOENIX, false, () -> Phoenix.withHealth(health));
    }

    public static MonsterTile ghostShip() {
        return new MonsterTile(SeaEvent.GHOST_SHIP, false, GhostShip::new);
    }

    public static MonsterTile pirateShip() {
        return new MonsterTile(SeaEvent.PIRATE_SHIP, false, PirateShip::new);
    }

    public static MonsterTile pirateShip(Random random) {
        int health = PirateShip.rollHealth(random);
        return new MonsterTile(SeaEvent.PIRATE_SHIP, false, () -> PirateShip.withHealth(health));
    }

    @Override
    public boolean isCompleted() {
        return this.monsterHere.isDead() && this.lootClaimed;
    }

    @Override
    protected void complete() {
        this.lootClaimed = true;
    }

    @Override
    protected Item item() {
        return null;
    }

    @Override
    protected Hazard hazard() {
        return null;
    }

    @Override
    protected Monster combatant() {
        return this.monsterHere;
    }

    @Override
    protected Reward reward() {
        return null;
    }

    @Override
    protected void handlePlayerFled() {
        this.monsterHere = monsterSpawner.get();
    }
}
