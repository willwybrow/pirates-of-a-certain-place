package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Resolves hazard tile behaviour against the player/world and returns generated log lines. */
public final class HazardEngine {
    private static final int ICEBERG_DAMAGE = 20;

    private final Random hazardRandom;

    public HazardEngine(Random hazardRandom) {
        this.hazardRandom = hazardRandom;
    }

    String resolve(SeaTile hazardTile, Player player, World world) {
        if (hazardTile.getHazard() == null) {
            return null;
        }
        switch (hazardTile.getHazard()) {
            case ICEBERG:
                return resolveIceberg(player);
            case WHIRLPOOL:
                return resolveWhirlpool(player, world);
            default:
                break;
        }
        return null;
    }

    private String resolveIceberg(Player player) {
        player.takeDamage(ICEBERG_DAMAGE);
        return "An iceberg gouged the hull for " + ICEBERG_DAMAGE + " damage.";
    }

    private String resolveWhirlpool(Player player, World world) {
        List<Treasure> capturedTreasures = player.capturedTreasureList();
        if (capturedTreasures.isEmpty()) {
            return "You daringly braved a terrifying whirlpool!";
        }

        Treasure lostTreasure = capturedTreasures.get(this.hazardRandom.nextInt(capturedTreasures.size()));
        boolean rehidden = world.rehideTreasureUnderUnexploredTile(lostTreasure, this.hazardRandom);
        if (!rehidden) {
            return "You daringly braved a terrifying whirlpool!";
        }

        player.loseTreasure(lostTreasure);
        return "A whirlpool swallowed the " + lostTreasure.displayName() + " and hid it anew.";
    }
}
