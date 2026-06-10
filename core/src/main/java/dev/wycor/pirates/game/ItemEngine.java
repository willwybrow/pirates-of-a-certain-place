package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Resolves item effects and returns transient UI cues to be surfaced by the game/UI. */
public final class ItemEngine {
    private static final int SEXTANT_RADIUS = 2;

    Resolution resolveEncounter(Item item, Hex encounterHex, World world) {
        switch (item) {
            case MAP:
                return resolveMap(encounterHex, world);
            case SEXTANT:
                return resolveSextant(encounterHex, world);
            case SPYGLASS:
                return Resolution.logOnly("Found a Spyglass. Choose a direction.");
            case TAR:
            default:
                return Resolution.empty();
        }
    }

    Resolution resolveSpyglassDirection(Hex playerPosition, Direction direction, World world) {
        Hex centre = firstUnexploredOrFarthestAlong(playerPosition, direction, world);
        revealCluster(centre, world);
        return Resolution.logOnly("Spyglass revealed waters to the " + direction.name() + ".");
    }

    private Resolution resolveMap(Hex encounterHex, World world) {
        Optional<Hex> nearestTreasure = nearestUnclaimedTreasure(encounterHex, world);
        if (nearestTreasure.isEmpty()) {
            return Resolution.logOnly("Map found no unclaimed treasure.");
        }

        Hex treasureHex = nearestTreasure.get();
        world.whatsAt(treasureHex).spy();

        ArrayList<TileHighlight> highlights = new ArrayList<>();
        highlights.add(new TileHighlight(treasureHex, TileHighlight.Color.GREEN));
        return Resolution.withHighlights("Map marked the nearest unclaimed treasure.", highlights);
    }

    private Resolution resolveSextant(Hex encounterHex, World world) {
        ArrayList<TileHighlight> highlights = new ArrayList<>();
        for (Hex hex : world.generatedHexes()) {
            if (hexDistance(encounterHex, hex) > SEXTANT_RADIUS) {
                continue;
            }

            SeaTile tile = world.whatsAt(hex);
            if (!isHazardOrMonster(tile.pendingEvent())) {
                continue;
            }

            tile.spy();
            highlights.add(new TileHighlight(hex, TileHighlight.Color.RED));
        }

        if (highlights.isEmpty()) {
            return Resolution.logOnly("Sextant found no nearby hazards or monsters.");
        }
        return Resolution.withHighlights("Sextant marked nearby hazards and monsters.", highlights);
    }

    private Optional<Hex> nearestUnclaimedTreasure(Hex encounterHex, World world) {
        return world.generatedHexes().stream()
            .filter(hex -> SeaEvent.treasureFor(world.whatsAt(hex).pendingEvent()).isPresent())
            .min(Comparator
                .comparingInt((Hex hex) -> hexDistance(encounterHex, hex))
                .thenComparingInt(Hex::q)
                .thenComparingInt(Hex::r));
    }

    private static boolean isHazardOrMonster(SeaEvent event) {
        switch (event) {
            case ICEBERG:
            case WHIRLPOOL:
            case GIANT_SQUID:
            case SEAWEED_MONSTER:
            case PHOENIX:
            case GHOST_SHIP:
            case PIRATE_SHIP:
                return true;
            default:
                return false;
        }
    }

    private Hex firstUnexploredOrFarthestAlong(Hex origin, Direction direction, World world) {
        Hex cursor = direction.move(origin);
        Hex fallback = null;

        while (World.isWithinWorld(cursor)) {
            fallback = cursor;
            if (!world.whatsAt(cursor).isSpied()) {
                return cursor;
            }
            cursor = direction.move(cursor);
        }

        return fallback != null ? fallback : origin;
    }

    private static void revealCluster(Hex centre, World world) {
        if (World.isWithinWorld(centre)) {
            world.whatsAt(centre).spy();
        }
        for (Hex neighbour : centre.neighbours()) {
            if (World.isWithinWorld(neighbour)) {
                world.whatsAt(neighbour).spy();
            }
        }
    }

    private static int hexDistance(Hex a, Hex b) {
        int dq = Math.abs(a.q() - b.q());
        int dr = Math.abs(a.r() - b.r());
        int ds = Math.abs(a.s() - b.s());
        return Math.max(dq, Math.max(dr, ds));
    }

    static final class Resolution {
        private final ArrayList<String> logLines;
        private final ArrayList<TileHighlight> highlights;

        private Resolution(ArrayList<String> logLines, ArrayList<TileHighlight> highlights) {
            this.logLines = logLines;
            this.highlights = highlights;
        }

        static Resolution empty() {
            return new Resolution(new ArrayList<>(), new ArrayList<>());
        }

        static Resolution logOnly(String logLine) {
            ArrayList<String> logLines = new ArrayList<>();
            logLines.add(logLine);
            return new Resolution(logLines, new ArrayList<>());
        }

        static Resolution withHighlights(String logLine, List<TileHighlight> highlights) {
            ArrayList<String> logLines = new ArrayList<>();
            logLines.add(logLine);
            return new Resolution(logLines, new ArrayList<>(highlights));
        }

        List<String> logLines() {
            return this.logLines;
        }

        List<TileHighlight> highlights() {
            return this.highlights;
        }
    }
}
