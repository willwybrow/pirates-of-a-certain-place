package dev.wycor.pirates;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.game.Sea;
import dev.wycor.pirates.game.SeaEvent;
import dev.wycor.pirates.game.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private static final float FLOATS_PER_PIXEL = 0.005f;
    public static final float SIXTEEN_PIXELS = 0.08f;
    public static final float THIRTY_TWO_PIXELS = 0.16f;
    private static final float SCREEN_WIDTH = 1.6f;
    private static final float SCREEN_HEIGHT = 1.0f;
    private static final float HEX_WIDTH = THIRTY_TWO_PIXELS;
    private static final float HEX_HEIGHT = THIRTY_TWO_PIXELS;
    private static final float MAP_UNIT_WIDTH = SIXTEEN_PIXELS;
    private static final float MAP_UNIT_HEIGHT = SIXTEEN_PIXELS;

    private SpriteBatch batch;
    private Viewport viewport;

    private final DrawableUI ui = new DrawableUI(SCREEN_WIDTH, SCREEN_HEIGHT, HEX_WIDTH, HEX_HEIGHT);

    private Texture seaHexture;
    private Texture krakenTexture;
    private Texture squidTexture;
    private Texture ghostTexture;
    private Texture islandTexture;
    private Texture stockedIslandTexture;
    private Texture fogOfWarTexture;
    private Texture shipTexture;
    private final Sea sea = new Sea();

    @Override
    public void create() {
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT);
        viewport.getCamera().position.set(0f, 0f, 0f);

        batch = new SpriteBatch();

        ui.create(sea);

        seaHexture = new Texture("sea_hex_32.png");
        krakenTexture = new Texture("seaweed_monster_1x_32.png");
        ghostTexture = new Texture("ghost_ship_1x_32.png");
        squidTexture = new Texture("giant_squid_1x_32.png");
        islandTexture = new Texture("island_empty_1x_32.png");
        stockedIslandTexture = new Texture("island_stocked_1x_32.png");
        fogOfWarTexture = new Texture("unexplored_hex_32.png");
        shipTexture = new Texture("hero_ship_1x_32.png");
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.08f, 0.25f, 0.3625f, 1f);

        repointCamera(dt);

        draw(dt);
        drawUi();
    }

    private void repointCamera(float dt) {
        Vector3 cameraTarget = centreOfHex(sea.currentPosition().neighbour(Direction.EAST).neighbour(Direction.EAST));
        viewport.getCamera().position.lerp(cameraTarget, 1.0f - (float)Math.exp(-5.0f * dt));
    }

    private void draw(float dt) {
        viewport.apply(); // Locks OpenGL to your game coordinates
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        sea.walkTheSpiral(13).forEach(exploredHex -> {
            drawAtHex(seaHexture, exploredHex);
            SeaTile whatsHere = sea.whatsAt(exploredHex);
            if (!whatsHere.isExplored()) {
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
                }
            } else {
                if (whatsHere.completedEvent() == SeaEvent.ISLAND) {
                    drawAtHex(islandTexture, exploredHex);
                }
            }
        });
        drawAtHex(shipTexture, sea.currentPosition());
        batch.end();
    }

    private void drawUi() {
        ui.draw();
    }


    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false); // true centers the camera
        ui.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        ui.dispose();
        seaHexture.dispose();
        krakenTexture.dispose();
        islandTexture.dispose();
        fogOfWarTexture.dispose();
        shipTexture.dispose();
    }

    public float boundingBoxX(Hex hex) {
        return (hex.q() + 1/2f * hex.r()) * HEX_WIDTH;
    }

    public float boundingBoxY(Hex hex) {
        return (3/4f * -hex.r()) * HEX_HEIGHT;
    }

    private Vector3 centreOfHex(Hex position) {
        return new Vector3(
            ((position.q() + 1/2f * position.r()) * HEX_WIDTH) + (HEX_WIDTH / 2),
            ((3/4f * -position.r()) * HEX_HEIGHT) + (HEX_HEIGHT / 2),
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
