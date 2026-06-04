package dev.wycor.pirates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.game.Sea;
import dev.wycor.pirates.ui.Cursive;

import static dev.wycor.pirates.DynamicDrawing.createSolidTexture;

public class DrawableStatus {
    private final Sea sea;
    private final float worldWidth;
    private final float worldHeight;

    private final int numberOfLines;

    private static final float LINE_HEIGHT = Main.SIXTEEN_PIXELS / 2;

    private Viewport viewport;
    private SpriteBatch batch;
    private Texture backgroundTexture;
    private Cursive cursive;

    public DrawableStatus(Sea sea, float worldWidth, float worldHeight) {
        this.sea = sea;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.numberOfLines = Math.round(worldHeight / LINE_HEIGHT);
    }

    public void create() {
        viewport = new FitViewport(worldWidth, worldHeight);
        batch = new SpriteBatch();
        backgroundTexture = createSolidTexture(0f, 0f, 0f, 0.85f);
        cursive = new Cursive();
        cursive.create();
    }

    public void draw() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(backgroundTexture, 0f, 0f, worldWidth, worldHeight);

        String[] logLines = sea.recentLog().toArray(new String[]{});

        for (int i = 0; i < Math.min(logLines.length, numberOfLines); i++) {
            cursive.write(batch, 0f, LINE_HEIGHT * i, logLines[i]);
        }

        batch.end();
    }

    public void resize(int width, int height) {
        int leftWidth = Math.round(width * (2f / 3f));
        int topHeight = Math.round(height * (2f / 3f));
        int bottomHeight = height - topHeight;

        viewport.update(leftWidth, bottomHeight, true);
        viewport.setScreenBounds(0, 0, leftWidth, bottomHeight);
    }

    public void dispose() {
        backgroundTexture.dispose();
        batch.dispose();
    }
}
