# AGENTS.md

## Scope
- Gradle multi-project libGDX game with three modules: `core`, `lwjgl3`, and `html` (see `settings.gradle`).

## Fast Commands (run from repo root)
- `./gradlew lwjgl3:run` - run the desktop app.
- `./gradlew lwjgl3:jar` - build the runnable desktop JAR at `lwjgl3/build/libs/`. This MUST pass after every change.
- `./gradlew html:dist` - build the deployable web output at `html/build/dist/`. This MUST pass after every change.
- `./gradlew html:superDev` - run GWT SuperDev flow for browser iteration.
- `./gradlew test` - run all Gradle test tasks (currently no `src/test` sources in this repo). This MUST pass after every change.

## Entrypoints and Module Boundaries
- Shared game logic entrypoint: `core/src/main/java/dev/wycor/pirates/Main.java`.
- Desktop launcher (`application.mainClass`): `lwjgl3/src/main/java/dev/wycor/pirates/lwjgl3/Lwjgl3Launcher.java`.
- Web launcher: `html/src/main/java/dev/wycor/pirates/gwt/GwtLauncher.java`.
- GWT module names are hardcoded in `html/build.gradle` (`dev.wycor.pirates.GdxDefinition` and `dev.wycor.pirates.GdxDefinitionSuperdev`); keep them aligned with package/module renames.

## Build and Asset Gotchas
- Root `build.gradle` wires `processResources` to `generateAssetList`, which regenerates `assets/assets.txt` from the `assets/` directory contents.
- `assets/assets.txt` is git-ignored; do not treat its presence/absence as a source change.
- Desktop `run` config in `lwjgl3/build.gradle` sets `workingDir` to `assets/`; relative runtime asset paths assume this.
- Desktop `run` also adds `--enable-native-access=ALL-UNNAMED`; preserve this when editing JVM args.
- Java is configured for source/target 11 in module builds; avoid Java >11 APIs unless build settings are updated.
- Gradle wrapper is pinned to `9.5.1` in `gradle/wrapper/gradle-wrapper.properties`.

## GWT Compatibility Safeguards
- Any code in `core/` must compile under GWT for the `html` target, not just desktop/JVM.
- Avoid Java APIs/features that are often missing in GWT/JRE emulation (for example `List.of(...)`, `Set.of(...)`, `Map.of(...)`, `List.copyOf(...)`, streams/APIs that depend on unsupported JDK internals).
- Prefer broadly compatible constructs such as `new ArrayList<>()`, `new HashMap<>()`, `Collections.emptyList()`, `Collections.unmodifiableList(...)`, etc.
- Treat `./gradlew html:dist` as a required verification step for every code change that touches shared/game logic or UI rendering.
- A change is not complete until `./gradlew html:dist` succeeds.

## External Reference
- libGDX wiki is available at `https://libgdx.com/wiki/`; use it for framework-specific behavior when repo sources are unclear.

## Game Design Principles
- High-level gameplay principles and balancing intent live in `GAME_DESIGN.md`.
- Keep mechanical changes aligned with those principles, especially around risk/reward and resource pressure.
