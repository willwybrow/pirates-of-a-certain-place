package dev.wycor.pirates.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.game.PlayerDetails;
import dev.wycor.pirates.game.Sea;

import static dev.wycor.pirates.ui.DynamicDrawing.createSolidTexture;

public class DrawableStatus {
    private final Sea sea;
    private final float worldWidth;
    private final float worldHeight;

    private final int numberOfLines;

    private static final float LINE_HEIGHT = BaseUI.SIXTEEN_PIXELS / 2;
    private static final float CURSIVE_LETTER_WIDTH = BaseUI.SIXTEEN_PIXELS / 4f;

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

        PlayerDetails player = sea.playerDetails();

        String healthLabel = "Health: " + player.health() + "/" + player.maxHealth();
        batch.setColor(0.95f, 0.2f, 0.2f, 1f);
        cursive.write(batch, 0f, worldHeight - LINE_HEIGHT, healthLabel);

        String foodLabel = "Food: " + player.food();
        float foodX = worldWidth - (foodLabel.length() * CURSIVE_LETTER_WIDTH);
        batch.setColor(0.95f, 0.72f, 0.22f, 1f);
        cursive.write(batch, Math.max(0f, foodX), worldHeight - LINE_HEIGHT, foodLabel);

        batch.setColor(Color.WHITE);

        String[] logLines = sea.recentLog().toArray(new String[]{});
        int visibleLogLines = Math.max(0, numberOfLines - 1);

        for (int i = 0; i < Math.min(logLines.length, visibleLogLines); i++) {
            cursive.write(batch, 0f, LINE_HEIGHT * i, logLines[i]);
        }

        batch.end();
    }

    public void resize(int screenX, int screenY, int screenWidth, int screenHeight) {
        int leftWidth = Math.round(screenWidth * (2f / 3f));
        int topHeight = Math.round(screenHeight * (2f / 3f));
        int bottomHeight = screenHeight - topHeight;

        viewport.update(leftWidth, bottomHeight, true);
        viewport.setScreenBounds(screenX, screenY, leftWidth, bottomHeight);
    }

    public void dispose() {
        backgroundTexture.dispose();
        batch.dispose();
    }
}
