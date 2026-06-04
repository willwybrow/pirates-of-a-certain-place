package dev.wycor.pirates.game;

import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Stream;

public class Sea {

    private Hex position;

    private final Map<Hex, SeaTile> generatedHexagons = new HashMap<>(500);
    private final ArrayDeque<String> log = new ArrayDeque<>();

    public Sea() {
        this.position = Hex.ORIGIN;
        this.generatedHexagons.put(this.position, SeaTile.startingSquare());
        this.goToAndExplore(Hex.ORIGIN);
        this.position.neighbours()
            .stream()
//            .map(Hex::neighbours)
//            .flatMap(List::stream)
//            .map(Hex::neighbours)
//            .flatMap(List::stream)
            .forEach(this::whatsAt);
//        Stream.of(
//            new Hex(-1, 0),
//            new Hex(2, -2)
//        ).forEach(this::whatsAt);
    }

    public Hex currentPosition() {
        return this.position;
    }

    public SeaTile whatsAt(Hex location) {
        return generatedHexagons.computeIfAbsent(location, hex -> SeaTile.generate());
    }

    public void go(Direction direction) {
        log.addFirst("Travelled " + direction);
        goToAndExplore(direction.move(this.position));
    }

    public void goToAndExplore(Hex destination) {
        SeaEvent whatsHere = whatsAt(destination).explore();
        log.addFirst("Encountered " + whatsHere + "!");
        this.position = destination;
        destination.neighbours().stream().map(this::whatsAt).forEach(SeaTile::explore);
    }

    public Set<Hex> explored() {
        return generatedHexagons.keySet();
    }

    public Stream<Hex> walkTheSpiral(int layers) {
        HashSet<Hex> hexes = new HashSet<>();
        var n = Math.abs(layers);

        for (int q = -n; q<= n; q++) {
            for (int r = Math.max(-n, -q-n); r <= Math.min(n, -q+n); r++) {
                hexes.add(new Hex(this.position.q() + q, this.position.r() + r));
            }
        }

        return hexes.stream();
    }

    public List<String> recentLog() {
        return new ArrayList<>(this.log);
    }
}
