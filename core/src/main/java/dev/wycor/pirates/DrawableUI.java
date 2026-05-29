package dev.wycor.pirates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static dev.wycor.pirates.DynamicDrawing.createSolidTexture;

public class DrawableUI {
    private static final float GRID_SQUARE = 1f;

    private Texture uiPanelBackgroundTexture;

    private Viewport uiViewport;
    private SpriteBatch uiBatch;

    public void create() {

        uiPanelBackgroundTexture = createSolidTexture(0f, 0f, 0f, 1f);

        uiViewport = new FitViewport(16f, 10f);
        uiBatch = new SpriteBatch();

    }

    public void draw() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        uiViewport.apply();
        uiBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        uiBatch.begin();
        uiBatch.draw(uiPanelBackgroundTexture, 10 * GRID_SQUARE, 0, 3 * GRID_SQUARE, 10 * GRID_SQUARE);
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
