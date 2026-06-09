package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.HazardTile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Resolves hazard tile behavior against the player/world and returns generated log lines. */
public final class HazardEngine {
    private static final int ICEBERG_DAMAGE = 20;

    private final Random hazardRandom;

    public HazardEngine(Random hazardRandom) {
        this.hazardRandom = hazardRandom;
    }

    List<String> resolve(HazardTile hazardTile, Player player, World world) {
        ArrayList<String> logLines = new ArrayList<>(1);

        switch (hazardTile.hazard()) {
            case ICEBERG:
                resolveIceberg(player, logLines);
                break;
            case WHIRLPOOL:
                resolveWhirlpool(player, world, logLines);
                break;
            default:
                break;
        }

        hazardTile.trigger();
        return logLines;
    }

    private static void resolveIceberg(Player player, List<String> logLines) {
        player.takeDamage(ICEBERG_DAMAGE);
        logLines.add("An iceberg gouged the hull for " + ICEBERG_DAMAGE + " damage.");
    }

    private void resolveWhirlpool(Player player, World world, List<String> logLines) {
        List<Treasure> capturedTreasures = player.capturedTreasureList();
        if (capturedTreasures.isEmpty()) {
            logLines.add("A whirlpool churned past, but you had no treasure to lose.");
            return;
        }

        Treasure lostTreasure = capturedTreasures.get(this.hazardRandom.nextInt(capturedTreasures.size()));
        boolean rehidden = world.rehideTreasureUnderUnexploredTile(lostTreasure, this.hazardRandom);
        if (!rehidden) {
            logLines.add("A whirlpool churned past, but there was nowhere left to hide your treasure.");
            return;
        }

        player.loseTreasure(lostTreasure);
        logLines.add("A whirlpool swallowed the " + lostTreasure.displayName() + " and hid it anew.");
    }
}
