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
import dev.wycor.pirates.game.Treasure;

import java.util.Map;

import static dev.wycor.pirates.ui.DynamicDrawing.createSolidTexture;

public class DrawableStatus {
    private static final int HEALTH_FIELD_WIDTH = 3;
    private static final int FOOD_FIELD_WIDTH = 3;
    private static final int ENEMY_HEALTH_FIELD_WIDTH = 3;

    private final Sea sea;
    private final float worldWidth;
    private final float worldHeight;

    private final int numberOfLines;

    private static final float LINE_HEIGHT = BaseUI.SIXTEEN_PIXELS / 2;
    private static final float CURSIVE_LETTER_WIDTH = BaseUI.SIXTEEN_PIXELS / 4f;
    private static final float TREASURE_ICON_SIZE = BaseUI.SIXTEEN_PIXELS;
    private static final float TREASURE_ICON_SPACING = BaseUI.SIXTEEN_PIXELS / 4f;

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
        BaseUI.loadTreasureTextures();
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

        String healthLabel = "Health: "
            + leftPad(Integer.toString(player.health()), HEALTH_FIELD_WIDTH)
            + "/"
            + leftPad(Integer.toString(player.maxHealth()), HEALTH_FIELD_WIDTH);
        batch.setColor(0.95f, 0.2f, 0.2f, 1f);
        cursive.write(batch, 0f, worldHeight - LINE_HEIGHT, healthLabel);

        String foodLabel = "Food: " + leftPad(Integer.toString(player.food()), FOOD_FIELD_WIDTH);
        float foodX = (worldWidth - (foodLabel.length() * CURSIVE_LETTER_WIDTH)) / 2f;
        batch.setColor(0.95f, 0.72f, 0.22f, 1f);
        cursive.write(batch, Math.max(0f, foodX), worldHeight - LINE_HEIGHT, foodLabel);

        sea.currentOpponentDetails().ifPresent(opponent -> {
            String opponentLabel = opponent.name()
                + ": "
                + leftPad(Integer.toString(opponent.health()), ENEMY_HEALTH_FIELD_WIDTH)
                + "/"
                + leftPad(Integer.toString(opponent.maxHealth()), ENEMY_HEALTH_FIELD_WIDTH);
            float opponentX = worldWidth - (opponentLabel.length() * CURSIVE_LETTER_WIDTH);
            batch.setColor(0.9f, 0.9f, 0.9f, 1f);
            cursive.write(batch, Math.max(0f, opponentX), worldHeight - LINE_HEIGHT, opponentLabel);
        });

        batch.setColor(Color.WHITE);

        drawTreasureProgress(player.capturedTreasures());

        String[] logLines = sea.recentLog().toArray(new String[]{});
        int visibleLogLines = Math.max(0, numberOfLines - 3);

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
        BaseUI.disposeTreasureTextures();
        batch.dispose();
    }

    private void drawTreasureProgress(Map<Treasure, Boolean> capturedTreasures) {
        Treasure[] treasures = Treasure.values();
        float iconsTotalWidth = (treasures.length * TREASURE_ICON_SIZE) + ((treasures.length - 1) * TREASURE_ICON_SPACING);
        float drawX = Math.max(0f, (worldWidth - iconsTotalWidth) / 2f);
        float drawY = worldHeight - LINE_HEIGHT - TREASURE_ICON_SIZE - (LINE_HEIGHT / 4f);

        for (Treasure treasure : treasures) {
            boolean captured = capturedTreasures.getOrDefault(treasure, false);
            if (captured) {
                batch.setColor(1f, 1f, 1f, 1f);
            } else {
                batch.setColor(0.4f, 0.4f, 0.4f, 0.6f);
            }

            batch.draw(BaseUI.treasureTexture(treasure), drawX, drawY, TREASURE_ICON_SIZE, TREASURE_ICON_SIZE);
            drawX += TREASURE_ICON_SIZE + TREASURE_ICON_SPACING;
        }

        batch.setColor(Color.WHITE);
    }

    private String leftPad(String value, int width) {
        if (value.length() >= width) {
            return value;
        }

        StringBuilder builder = new StringBuilder(width);
        for (int i = value.length(); i < width; i++) {
            builder.append(' ');
        }
        builder.append(value);
        return builder.toString();
    }
}
