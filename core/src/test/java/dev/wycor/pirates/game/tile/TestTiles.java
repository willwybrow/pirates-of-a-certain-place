package dev.wycor.pirates.game.tile;

import dev.wycor.pirates.game.Monster;
import dev.wycor.pirates.game.SeaEvent;

import java.util.function.Supplier;

public class TestTiles {
    public static SeaTile testMonster(SeaEvent seaEvent, boolean explored, Supplier<Monster> monsterSupplier) {
        return new MonsterTile(seaEvent, explored, monsterSupplier);
    }
}
