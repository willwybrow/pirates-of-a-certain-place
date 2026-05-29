package dev.wycor.pirates;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import dev.wycor.pirates.game.Sea;
import dev.wycor.pirates.game.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private static final float SCREEN_WIDTH = 16f;
    private static final float SCREEN_HEIGHT = 10f;
    private static final float HEX_WIDTH = 1f;
    private static final float HEX_HEIGHT = 1f;

    private SpriteBatch batch;
    private FitViewport viewport;

    private DrawableUI ui = new DrawableUI();

    private Texture seaHexture;
    private Texture krakenTexture;
    private Texture islandTexture;
    private Texture fogOfWarTexture;
    private Texture shipTexture;
    private Sea sea = new Sea();

    @Override
    public void create() {
        viewport = new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT);
        batch = new SpriteBatch();

        ui.create();

        seaHexture = new Texture("sea_hex_16.png");
        krakenTexture = new Texture("sea_monster_kraken_16.png");
        islandTexture = new Texture("desert_island_16.png");
        fogOfWarTexture = new Texture("unexplored_hex_16.png");
        shipTexture = new Texture("basic_ship.png");

    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.25f, 0.3625f, 1f);

        input();

        draw(batch);
        drawUi();
    }

    private void draw(SpriteBatch batch) {
        viewport.apply(); // Locks OpenGL to your game coordinates
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        sea.walkTheSpiral((int)Math.max(SCREEN_HEIGHT, SCREEN_WIDTH)).forEach(exploredHex -> {
            batch.draw(seaHexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
            SeaTile whatsHere = sea.whatsAt(exploredHex);
            if (!whatsHere.isExplored()) {
                batch.draw(fogOfWarTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
            } else {
                switch (whatsHere.pendingEvent()) {
                    case ISLAND:
                        batch.draw(islandTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                        break;
                    case KRAKEN:
                        batch.draw(krakenTexture, boundingBoxX(exploredHex), boundingBoxY(exploredHex), HEX_WIDTH, HEX_HEIGHT);
                        break;
                }
            }
        });
        batch.draw(shipTexture, boundingBoxX(sea.currentPosition()), boundingBoxY(sea.currentPosition()), HEX_WIDTH, HEX_HEIGHT);
        batch.end();
    }

    private void drawUi() {
        ui.draw();
    }

    private TextButton createControlButton(String text, Runnable action, Skin skin) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        return button;
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
        viewport.update(width, height, true); // true centers the camera
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
        return (SCREEN_WIDTH /2f - HEX_WIDTH) + hex.q() + 1/2f * hex.r();
    }

    public float boundingBoxY(Hex hex) {
        return ((SCREEN_HEIGHT /2f - HEX_HEIGHT)) + 3/4f * -hex.r();
    }
}
