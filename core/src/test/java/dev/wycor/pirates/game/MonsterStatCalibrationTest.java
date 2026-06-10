package dev.wycor.pirates.game;

import dev.wycor.pirates.game.monster.GhostShip;
import dev.wycor.pirates.game.monster.GiantSquid;
import dev.wycor.pirates.game.monster.Phoenix;
import dev.wycor.pirates.game.monster.PirateShip;
import dev.wycor.pirates.game.monster.SeaweedMonster;
import dev.wycor.pirates.game.tile.MonsterTile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class MonsterStatCalibrationTest {

    @Test
    void giantSquidHealthRollsWithinEmpiricalRange() {
        assertRolledHealthRange(MonsterTile::giantSquid, GiantSquid.MIN_HEALTH, GiantSquid.MAX_HEALTH);
    }

    @Test
    void seaweedMonsterHealthRollsWithinEmpiricalRange() {
        assertRolledHealthRange(MonsterTile::seaweedMonster,
            SeaweedMonster.MIN_HEALTH,
            SeaweedMonster.MAX_HEALTH);
    }

    @Test
    void phoenixHealthRollsWithinEmpiricalRange() {
        assertRolledHealthRange(MonsterTile::phoenix, Phoenix.MIN_HEALTH, Phoenix.MAX_HEALTH);
    }

    @Test
    void pirateShipHealthRollsWithinEmpiricalRange() {
        assertRolledHealthRange(MonsterTile::pirateShip, PirateShip.MIN_HEALTH, PirateShip.MAX_HEALTH);
    }

    @Test
    void ghostShipHealthRollsWithinEmpiricalRange() {
        assertRolledHealthRange(MonsterTile::ghostShip, GhostShip.MIN_HEALTH, GhostShip.MAX_HEALTH);
    }

    private static void assertRolledHealthRange(Function<Random, MonsterTile> tileFactory,
                                                int minHealth,
                                                int maxHealth) {
        List<Integer> sampledHealth = sampleHealth(tileFactory, 200);
        assertThat(sampledHealth).allMatch(health -> health >= minHealth && health <= maxHealth);
        assertThat(new HashSet<>(sampledHealth)).contains(minHealth, maxHealth);
    }

    private static List<Integer> sampleHealth(Function<Random, MonsterTile> tileFactory, int sampleCount) {
        ArrayList<Integer> sampledHealth = new ArrayList<>(sampleCount);
        Random random = new Random(0L);
        for (int i = 0; i < sampleCount; i++) {
            MonsterTile tile = tileFactory.apply(random);
            sampledHealth.add(tile.getCombatant().maxHealth());
        }
        return sampledHealth;
    }
}
