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
        shipTexture = new Texture("heroic_pirate_ship_16.png");
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.08f, 0.25f, 0.3625f, 1f);

        input();

        repointCamera(dt);

        draw(dt);
        drawUi();
    }

    private void repointCamera(float dt) {
        Vector3 cameraTarget = new Vector3(boundingBoxX(sea.currentPosition()) + 3 * HEX_WIDTH, boundingBoxY(sea.currentPosition()), 0f);
        viewport.getCamera().position.lerp(cameraTarget, 1.0f - (float)Math.exp(-5.0f * dt));
    }

    private void draw(float dt) {
        viewport.apply(); // Locks OpenGL to your game coordinates
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        sea.walkTheSpiral(13).forEach(exploredHex -> {
            // batch.draw(seaHexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
            drawAtHex(seaHexture, exploredHex);
            SeaTile whatsHere = sea.whatsAt(exploredHex);
            if (!whatsHere.isExplored()) {
                // batch.draw(fogOfWarTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                drawAtHex(fogOfWarTexture, exploredHex);
            } else if (!whatsHere.isCompleted()) {
                switch (whatsHere.pendingEvent()) {
                    case ISLAND:
                        // batch.draw(stockedIslandTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                        drawAtHex(stockedIslandTexture, exploredHex);
                        break;
                    case KRAKEN:
                        // batch.draw(krakenTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                        drawAtHex(krakenTexture, exploredHex);
                        break;
                    case SQUID:
                        // batch.draw(squidTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                        drawAtHex(squidTexture, exploredHex);
                        break;
                    case GHOST:
                        // batch.draw(ghostTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                        drawAtHex(ghostTexture, exploredHex);
                        break;
                }
            } else {
                if (whatsHere.completedEvent() == SeaEvent.ISLAND) {
                    // batch.draw(islandTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                    drawAtHex(islandTexture, exploredHex);
                }
            }
        });
        // batch.draw(shipTexture, boundingBoxX(sea.currentPosition()), boundingBoxY(sea.currentPosition()), HEX_WIDTH, HEX_HEIGHT);
        drawAtHex(shipTexture, sea.currentPosition());
        batch.end();
    }

    private void drawUi() {
        ui.draw();
    }

    private void input() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            sea.go(Direction.NORTHEAST);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            sea.go(Direction.EAST);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            sea.go(Direction.SOUTHEAST);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            sea.go(Direction.SOUTHWEST);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            sea.go(Direction.WEST);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            sea.go(Direction.NORTHWEST);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            sea.whatsAt(sea.currentPosition()).complete();
        }
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

    private void drawAtHex(Texture texture, Hex drawAt) {
        float width = texture.getWidth() * FLOATS_PER_PIXEL;
        float height = texture.getHeight() * FLOATS_PER_PIXEL;

        float centreX = ((drawAt.q() + 1/2f * drawAt.r()) * HEX_WIDTH) + (HEX_WIDTH / 2);
        float centreY = ((3/4f * -drawAt.r()) * HEX_HEIGHT) + (HEX_HEIGHT / 2);

        float bottomX = centreX - (width / 2f);
        float bottomY = centreY - (height / 2f);

        batch.draw(texture, bottomX, bottomY, width, height);
    }
}
