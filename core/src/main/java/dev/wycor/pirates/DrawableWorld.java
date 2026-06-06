package dev.wycor.pirates;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.game.Sea;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.SeaTile;
import dev.wycor.pirates.geometry.Hex;
import dev.wycor.pirates.ui.Cursive;

import static dev.wycor.pirates.DynamicDrawing.createSolidTexture;

public class DrawableWorld {
    private static final float FLOATS_PER_PIXEL = 0.005f;

    private final Sea sea;
    private final float worldWidth;
    private final float worldHeight;
    private final float hexWidth;
    private final float hexHeight;

    private Viewport viewport;
    private SpriteBatch batch;

    private Texture seaTexture;
    private Texture krakenTexture;
    private Texture squidTexture;
    private Texture ghostTexture;
    private Texture islandTexture;
    private Texture stockedIslandTexture;
    private Texture fogOfWarTexture;
    private Texture shipTexture;
    private Texture gameOverOverlayTexture;
    private Cursive cursive;

    public DrawableWorld(Sea sea, float worldWidth, float worldHeight, float hexWidth, float hexHeight) {
        this.sea = sea;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.hexWidth = hexWidth;
        this.hexHeight = hexHeight;
    }

    public void create() {
        viewport = new FitViewport(worldWidth, worldHeight);
        batch = new SpriteBatch();

        seaTexture = new Texture("sea_hex_32.png");
        krakenTexture = new Texture("seaweed_monster_1x_32.png");
        ghostTexture = new Texture("ghost_ship_1x_32.png");
        squidTexture = new Texture("giant_squid_1x_32.png");
        islandTexture = new Texture("island_empty_1x_32.png");
        stockedIslandTexture = new Texture("island_stocked_1x_32.png");
        fogOfWarTexture = new Texture("unexplored_hex_32.png");
        shipTexture = new Texture("hero_ship_1x_32.png");
        gameOverOverlayTexture = createSolidTexture(0.5f, 0.5f, 0.5f, 0.5f);
        cursive = new Cursive();
        cursive.create();

        viewport.getCamera().position.set(centreOfHex(cameraFocusHex()));
        viewport.getCamera().update();
    }

    public void draw(float dt) {
        repointCamera(dt);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        sea.walkTheSpiral(13).forEach(exploredHex -> {
            drawAtHex(seaTexture, exploredHex);
            SeaTile whatsHere = sea.whatsAt(exploredHex);
            if (!whatsHere.isSpied()) {
                drawAtHex(fogOfWarTexture, exploredHex);
            } else if (!whatsHere.isCompleted()) {
                switch (whatsHere.pendingEvent()) {
                    case ISLAND:
                        drawAtHex(stockedIslandTexture, exploredHex);
                        break;
                    case KRAKEN:
                        drawAtHex(krakenTexture, exploredHex);
                        break;
                    case SQUID:
                        drawAtHex(squidTexture, exploredHex);
                        break;
                    case GHOST:
                        drawAtHex(ghostTexture, exploredHex);
                        break;
                    default:
                        break;
                }
            } else if (whatsHere.completedEvent() == SeaEvent.ISLAND) {
                drawAtHex(islandTexture, exploredHex);
            }
        });
        drawAtHex(shipTexture, sea.playerDetails().position());

        if (sea.isGameOver()) {
            Vector3 cameraPosition = viewport.getCamera().position;
            float overlayX = cameraPosition.x - worldWidth / 2f;
            float overlayY = cameraPosition.y - worldHeight / 2f;
            batch.draw(gameOverOverlayTexture, overlayX, overlayY, worldWidth, worldHeight);

            String gameOverLabel = "GAME OVER";
            float letterWidth = Main.SIXTEEN_PIXELS / 4f;
            float textX = cameraPosition.x - (gameOverLabel.length() * letterWidth) / 2f;
            float textY = cameraPosition.y;
            cursive.write(batch, textX, textY, gameOverLabel);
        }

        batch.end();
    }

    public void resize(int screenX, int screenY, int screenWidth, int screenHeight) {
        int leftWidth = Math.round(screenWidth * (2f / 3f));
        int topHeight = Math.round(screenHeight * (2f / 3f));
        int topY = screenY + (screenHeight - topHeight);

        viewport.update(leftWidth, topHeight, false);
        viewport.setScreenBounds(screenX, topY, leftWidth, topHeight);
    }

    public void dispose() {
        batch.dispose();
        seaTexture.dispose();
        krakenTexture.dispose();
        squidTexture.dispose();
        ghostTexture.dispose();
        islandTexture.dispose();
        stockedIslandTexture.dispose();
        fogOfWarTexture.dispose();
        shipTexture.dispose();
        gameOverOverlayTexture.dispose();
    }

    private void repointCamera(float dt) {
        Vector3 cameraTarget = centreOfHex(cameraFocusHex());
        viewport.getCamera().position.lerp(cameraTarget, 1.0f - (float) Math.exp(-5.0f * dt));
    }

    private Hex cameraFocusHex() {
        return sea.getPlayerDestination().orElseGet(() -> sea.playerDetails().position());
    }

    private Vector3 centreOfHex(Hex position) {
        return new Vector3(
            ((position.q() + 0.5f * position.r()) * hexWidth) + (hexWidth / 2f),
            ((0.75f * -position.r()) * hexHeight) + (hexHeight / 2f),
            0f
        );
    }

    private void drawAtHex(Texture texture, Hex drawAt) {
        float width = texture.getWidth() * FLOATS_PER_PIXEL;
        float height = texture.getHeight() * FLOATS_PER_PIXEL;

        Vector3 hexCentre = centreOfHex(drawAt);

        float bottomX = hexCentre.x - (width / 2f);
        float bottomY = hexCentre.y - (height / 2f);

        batch.draw(texture, bottomX, bottomY, width, height);
    }
}
