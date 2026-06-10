package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.HazardTile;
import dev.wycor.pirates.game.tile.EmptyTile;
import dev.wycor.pirates.game.tile.ItemTile;
import dev.wycor.pirates.game.tile.MonsterTile;
import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.game.tile.TreasureTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class GameItemTest {

    @Test
    void tarRestoresHealthWhenEncountered() {
        Hex origin = Hex.ORIGIN;
        Hex icebergHex = Direction.EAST.move(origin);
        Hex tarHex = Direction.WEST.move(origin);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(icebergHex, HazardTile.iceberg());
                generated.put(tarHex, ItemTile.tar());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);
        game.recalculateGameState(2_000L);
        assertThat(game.playerDetails().health()).isEqualTo(80);

        game.attemptToTravel(Direction.WEST, 3_000L);
        game.attemptToTravel(Direction.WEST, 4_000L);

        assertThat(game.playerDetails().position()).isEqualTo(origin);
        assertThat(game.getPlayerDestination()).contains(tarHex);
        assertThat(game.playerDetails().health()).isEqualTo(80);

        game.recalculateGameState(6_000L);

        assertThat(game.playerDetails().position()).isEqualTo(tarHex);
        assertThat(game.playerDetails().health()).isEqualTo(95);
    }

    @Test
    void mapRevealsNearestUnclaimedTreasureAndSetsTemporaryCameraFocus() {
        Hex origin = Hex.ORIGIN;
        Hex mapHex = Direction.EAST.move(origin);
        Hex nearestTreasure = Direction.EAST.move(mapHex);
        Hex distantTreasure = Direction.WEST.move(Direction.WEST.move(origin));

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(mapHex, ItemTile.map());
                generated.put(nearestTreasure, TreasureTile.emeraldOfHope());
                generated.put(distantTreasure, TreasureTile.goldenSwordOfYr());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        assertThat(game.playerDetails().position()).isEqualTo(origin);
        assertThat(game.getPlayerDestination()).contains(mapHex);
        assertThat(game.whatsAt(mapHex).pendingEvent()).isEqualTo(SeaEvent.MAP);
        assertThat(game.whatsAt(mapHex).isCompleted()).isFalse();
        assertThat(game.whatsAt(nearestTreasure).isSpied()).isFalse();
        assertThat(game.whatsAt(distantTreasure).isSpied()).isFalse();
        assertThat(game.transientFocusHex(1L)).isEmpty();
        assertThat(game.activeTileHighlights()).isEmpty();

        game.recalculateGameState(2_000L);

        assertThat(game.playerDetails().position()).isEqualTo(origin);
        assertThat(game.getPlayerDestination()).contains(mapHex);
        assertThat(game.whatsAt(mapHex).pendingEvent()).isEqualTo(SeaEvent.MAP);
        assertThat(game.whatsAt(nearestTreasure).isSpied()).isTrue();
        assertThat(game.whatsAt(distantTreasure).isSpied()).isFalse();
        assertThat(game.transientFocusHex(2_000L)).contains(nearestTreasure);
        assertThat(game.activeTileHighlights())
            .extracting(TileHighlight::hex, TileHighlight::color)
            .containsExactly(org.assertj.core.groups.Tuple.tuple(nearestTreasure, TileHighlight.Color.GREEN));

        game.recalculateGameState(4_000L);
        assertThat(game.playerDetails().position()).isEqualTo(mapHex);
        assertThat(game.getPlayerDestination()).isEmpty();
        assertThat(game.whatsAt(mapHex).isCompleted()).isTrue();
        assertThat(game.transientFocusHex(4_000L)).isEmpty();
        assertThat(game.activeTileHighlights()).isEmpty();
    }

    @Test
    void sextantRevealsNearbyHazardsAndMonstersWithinTwoTiles() {
        Hex origin = Hex.ORIGIN;
        Hex sextantHex = Direction.EAST.move(origin);
        Hex nearMonsterHex = Direction.EAST.move(sextantHex);
        Hex nearHazardHex = Direction.NORTHEAST.move(sextantHex);
        Hex farHazardHex = Direction.WEST.move(Direction.WEST.move(origin));

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(sextantHex, ItemTile.sextant());
                generated.put(nearMonsterHex, MonsterTile.giantSquid(new Random(0L)));
                generated.put(nearHazardHex, HazardTile.iceberg());
                generated.put(farHazardHex, HazardTile.whirlpool());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        assertThat(game.playerDetails().position()).isEqualTo(origin);
        assertThat(game.getPlayerDestination()).contains(sextantHex);
        assertThat(game.whatsAt(sextantHex).pendingEvent()).isEqualTo(SeaEvent.SEXTANT);
        assertThat(game.whatsAt(sextantHex).isCompleted()).isFalse();
        assertThat(game.whatsAt(nearMonsterHex).isSpied()).isFalse();
        assertThat(game.whatsAt(nearHazardHex).isSpied()).isFalse();
        assertThat(game.activeTileHighlights()).isEmpty();

        game.recalculateGameState(2_000L);

        assertThat(game.whatsAt(nearMonsterHex).isSpied()).isTrue();
        assertThat(game.whatsAt(nearHazardHex).isSpied()).isTrue();
        assertThat(game.whatsAt(farHazardHex).isSpied()).isFalse();
        assertThat(game.playerDetails().position()).isEqualTo(sextantHex);
        assertThat(game.whatsAt(sextantHex).isCompleted()).isTrue();
        assertThat(game.activeTileHighlights())
            .extracting(TileHighlight::hex, TileHighlight::color)
            .contains(
                org.assertj.core.groups.Tuple.tuple(nearMonsterHex, TileHighlight.Color.RED),
                org.assertj.core.groups.Tuple.tuple(nearHazardHex, TileHighlight.Color.RED)
            );
    }

    @Test
    void spyglassConsumesNextDirectionInputToRevealClusterWithoutMovement() {
        Hex origin = Hex.ORIGIN;
        Hex spyglassHex = Direction.EAST.move(origin);
        Hex revealCentre = Direction.EAST.move(spyglassHex);
        Hex revealNeighbour = Direction.NORTHEAST.move(revealCentre);

        Game game = new TestGameFactory() {
            @Override
            public World generate() {
                HashMap<Hex, SeaTile> generated = new HashMap<>();
                generated.put(spyglassHex, ItemTile.spyglass());
                generated.put(revealCentre, EmptyTile.generate());
                generated.put(revealNeighbour, EmptyTile.generate());
                return new World(generated);
            }
        }.createGame();

        game.attemptToTravel(Direction.EAST, 1L);

        assertThat(game.isAwaitingSpyglassDirection()).isTrue();
        assertThat(game.playerDetails().position()).isEqualTo(origin);
        assertThat(game.playerDetails().food()).isEqualTo(20);
        assertThat(game.getPlayerDestination()).contains(spyglassHex);
        assertThat(game.whatsAt(spyglassHex).pendingEvent()).isEqualTo(SeaEvent.SPYGLASS);
        assertThat(game.whatsAt(spyglassHex).isCompleted()).isFalse();
        assertThat(game.whatsAt(revealCentre).isSpied()).isFalse();
        assertThat(game.whatsAt(revealNeighbour).isSpied()).isFalse();

        game.attemptToTravel(Direction.EAST, 2L);

        assertThat(game.isAwaitingSpyglassDirection()).isFalse();
        assertThat(game.playerDetails().position()).isEqualTo(origin);
        assertThat(game.playerDetails().food()).isEqualTo(20);
        assertThat(game.getPlayerDestination()).contains(spyglassHex);
        assertThat(game.whatsAt(spyglassHex).isCompleted()).isFalse();
        assertThat(game.whatsAt(revealCentre).isSpied()).isTrue();
        assertThat(game.whatsAt(revealNeighbour).isSpied()).isTrue();

        game.recalculateGameState(2_000L);

        assertThat(game.playerDetails().position()).isEqualTo(spyglassHex);
        assertThat(game.playerDetails().food()).isEqualTo(19);
        assertThat(game.getPlayerDestination()).isEmpty();
        assertThat(game.whatsAt(spyglassHex).isCompleted()).isTrue();
    }
}
