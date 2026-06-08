package dev.wycor.pirates.game;

import dev.wycor.pirates.game.tile.SeaTile;
import dev.wycor.pirates.geometry.Hex;

import java.util.List;
import java.util.Optional;

/**
 * Read-only view of the current game, intended for UI rendering and input gating. It exposes only
 * derived snapshots of the computed state, never the mutable game internals.
 */
public interface GameView {

    PlayerDetails playerDetails();

    boolean isGameOver();

    String gameOverMessage();

    Optional<Hex> getPlayerDestination();

    boolean hasActiveCombat();

    Optional<CombatantDetails> currentOpponentDetails();

    SeaTile whatsAt(Hex location);

    List<Hex> generatedHexes();

    List<String> recentLog();
}
