package dev.wycor.pirates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.ui.Font;

import static dev.wycor.pirates.DynamicDrawing.createSolidTexture;

public class DrawableUI {
    private final float gridSquare;

    private Texture uiPanelBackgroundTexture;

    private final Font cursive;

    private Viewport uiViewport;
    private SpriteBatch uiBatch;

    private final float width;
    private final float height;

    public DrawableUI(float width, float height) {
        this.width = width;
        this.height = height;

        this.gridSquare = width / 100f;
        this.cursive = new Font(gridSquare);
    }

    public void create() {

        uiPanelBackgroundTexture = createSolidTexture(0f, 0f, 0f, 1f);

        cursive.create();

        uiViewport = new FitViewport(width, height);
        uiBatch = new SpriteBatch();

    }

    public void draw() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        uiViewport.apply();
        uiBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        uiBatch.begin();
        uiBatch.draw(uiPanelBackgroundTexture, 75 * gridSquare, 0, 25 * gridSquare, 100 * gridSquare);
        cursive.write(uiBatch, 80 * gridSquare, 50 * gridSquare, " Pirates! @ ^_^");
        uiBatch.end();
    }

    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    public void dispose() {
        uiPanelBackgroundTexture.dispose();
        uiBatch.dispose();
    }
}
