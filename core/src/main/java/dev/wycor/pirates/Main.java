package dev.wycor.pirates;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.game.Sea;
import dev.wycor.pirates.game.SeaTile;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.geometry.Hex;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 16f;
    private static final float WORLD_HEIGHT = 10f;
    private static final float UI_WIDTH = WORLD_WIDTH * 50f;
    private static final float UI_HEIGHT = WORLD_HEIGHT * 50f;
    private static final float HEX_WIDTH = 1f;
    private static final float HEX_HEIGHT = 1f;

    private SpriteBatch batch;
    private FitViewport viewport;

    private Viewport uiViewport;
    private SpriteBatch uiBatch;
    private Stage uiStage;

    private Texture seaHexture;
    private Texture krakenTexture;
    private Texture islandTexture;
    private Texture fogOfWarTexture;
    private Texture shipTexture;
    private Texture uiPanelBackgroundTexture;
    private Sea sea = new Sea();

    @Override
    public void create() {
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        batch = new SpriteBatch();

        uiViewport = new FitViewport(UI_WIDTH, UI_HEIGHT);
        uiBatch = new SpriteBatch();
        uiStage = new Stage(uiViewport, uiBatch);

        Gdx.input.setInputProcessor(uiStage);

        seaHexture = new Texture("sea_hex_16.png");
        krakenTexture = new Texture("sea_monster_kraken_16.png");
        islandTexture = new Texture("desert_island_16.png");
        fogOfWarTexture = new Texture("unexplored_hex_16.png");
        shipTexture = new Texture("basic_ship.png");

        Skin skin = new Skin(new FileHandle("skin/commodore64/skin/uiskin.json"));
        Table rootTable = new Table();
        rootTable.setFillParent(true);
        rootTable.top();
        // rootTable.pad(8f);

        Table shipsLog = new Table();
        Table controls = new Table();
        Table status = new Table();

        uiPanelBackgroundTexture = createSolidTexture(0f, 0f, 0f, 1f);
        Drawable panelBackground = new TextureRegionDrawable(new TextureRegion(uiPanelBackgroundTexture));
        status.setBackground(panelBackground);
        controls.setBackground(panelBackground);
        shipsLog.setBackground(panelBackground);

        status.defaults().left().padBottom(4f);
        status.add(new Label("Status", skin)).left().row();
        status.add(new Label("Hull: 5", skin)).left().row();
        status.add(new Label("Crew: 12", skin)).left().row();
        status.add(new Label("Petril: 4", skin)).left().row();

        controls.defaults().width(72f).height(28f).pad(3f);
        controls.add(createControlButton("NW", () -> sea.go(Direction.NORTHWEST), skin));
        controls.add(createControlButton("NE", () -> sea.go(Direction.NORTHEAST), skin)).colspan(2);
        controls.row();
        controls.add(createControlButton("W", () -> sea.go(Direction.WEST), skin));
        controls.add(createControlButton("@", () -> sea.whatsAt(sea.currentPosition()).complete(), skin));
        controls.add(createControlButton("E", () -> sea.go(Direction.EAST), skin));
        controls.row();
        controls.add(createControlButton("SW", () -> sea.go(Direction.SOUTHWEST), skin));
        controls.add(createControlButton("SE", () -> sea.go(Direction.SOUTHEAST), skin)).colspan(2);

        rootTable.add().expand().fill();
        rootTable.add(status).width(Value.percentWidth(0.16f, rootTable)).expandY().fillY().top();
        rootTable.add(controls).width(Value.percentWidth(0.25f, rootTable)).expandY().fillY().top();

        rootTable.row();

        rootTable.add(shipsLog)
                .colspan(3)
                .height(Value.percentHeight(0.25f, rootTable))
                .expandX()
                .fillX()
                .bottom();

        shipsLog.add(new Label("Embarked", skin)).left().row();
        shipsLog.add(new Label("Sailed a bit, was fun", skin)).left().row();
        shipsLog.add(new Label("mildly shat the bed", skin)).left().row();

        uiStage.addActor(rootTable);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.08f, 0.25f, 0.3625f, 1f);

        input();

        draw(batch);

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        drawUi();

    }

    private void draw(SpriteBatch batch) {
        viewport.apply(); // Locks OpenGL to your game coordinates
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        sea.walkTheSpiral((int)Math.max(WORLD_HEIGHT, WORLD_WIDTH)).forEach(exploredHex -> {
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
        uiViewport.apply();
        uiBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        uiStage.act();
        uiStage.draw();
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

    private Texture createSolidTexture(float red, float green, float blue, float alpha) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(red, green, blue, alpha);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
        uiViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        uiBatch.dispose();
        uiStage.dispose();
        seaHexture.dispose();
        krakenTexture.dispose();
        islandTexture.dispose();
        fogOfWarTexture.dispose();
        shipTexture.dispose();
        uiPanelBackgroundTexture.dispose();
    }

    public float boundingBoxX(Hex hex) {
        return (WORLD_WIDTH/2f - HEX_WIDTH) + hex.q() + 1/2f * hex.r();
    }

    public float boundingBoxY(Hex hex) {
        return ((WORLD_HEIGHT/2f - HEX_HEIGHT)) + 3/4f * -hex.r();
    }
}
