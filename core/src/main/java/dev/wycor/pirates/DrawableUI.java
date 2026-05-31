package dev.wycor.pirates;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import dev.wycor.pirates.game.Sea;
import dev.wycor.pirates.geometry.Direction;
import dev.wycor.pirates.ui.Cursive;

import java.util.List;
import java.util.function.Consumer;

import static dev.wycor.pirates.DynamicDrawing.createSolidTexture;

public class DrawableUI {
    private final float gridSquare;
    private final float unitWidth;
    private final float unitHeight;

    private Texture uiPanelBackgroundTexture;
    private Texture button;
    private Texture arrowNorthEast;
    private Texture arrowEast;
    private Texture arrowSouthEast;
    private Texture arrowNorthWest;
    private Texture arrowWest;
    private Texture arrowSouthWest;

    private final Cursive cursive;

    private Viewport uiViewport;
    private SpriteBatch uiBatch;

    private final float screenWidth;
    private final float screenHeight;
    private List<DirectionButton> directionButtons;
    private Sea sea;

    public DrawableUI(float screenWidth, float screenHeight, float unitWidth, float unitHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.gridSquare = screenWidth / 100f;
        this.unitWidth = unitWidth;
        this.unitHeight = unitHeight;
        this.cursive = new Cursive();
    }

    public void create(Sea forSea) {
        this.sea = forSea;
        uiPanelBackgroundTexture = createSolidTexture(0f, 0f, 0f, 1f);
        button = new Texture("button_up_16.png");
        arrowNorthEast = new Texture("arrow_upright_16.png");
        arrowEast = new Texture("arrow_right_16.png");
        arrowSouthEast = new Texture("arrow_downright_16.png");
        arrowNorthWest = new Texture("arrow_upleft_16.png");
        arrowWest = new Texture("arrow_left_16.png");
        arrowSouthWest = new Texture("arrow_downleft_16.png");

        cursive.create();

        uiViewport = new FitViewport(screenWidth, screenHeight);
        uiBatch = new SpriteBatch();

        // the seven movement buttons

        var topMiddleX = 84 * gridSquare;
        var topMiddleY = 50 * gridSquare;

        this.directionButtons = List.of(
            new DirectionButton(button, arrowNorthWest, topMiddleX - 0.67f * unitWidth, topMiddleY - unitHeight, unitWidth, unitHeight, sea -> sea.go(Direction.NORTHWEST)),
            new DirectionButton(button, arrowNorthEast, topMiddleX + 0.67f * unitWidth, topMiddleY - unitHeight, unitWidth, unitHeight, sea -> sea.go(Direction.NORTHEAST)),
            new DirectionButton(button, arrowWest, topMiddleX - 1.2f * unitWidth, topMiddleY - 2 * unitHeight, unitWidth, unitHeight, sea -> sea.go(Direction.WEST)),
            new DirectionButton(button, button, topMiddleX, topMiddleY - 2 * unitHeight, unitWidth, unitHeight, sea -> sea.whatsAt(sea.currentPosition()).complete()), // TODO -- middle button??
            new DirectionButton(button, arrowEast, topMiddleX + 1.2f * unitWidth, topMiddleY - 2  * unitHeight, unitWidth, unitHeight, sea -> sea.go(Direction.EAST)),
            new DirectionButton(button, arrowSouthWest, topMiddleX - 0.67f * unitWidth, topMiddleY - 3 * unitHeight, unitWidth, unitHeight, sea -> sea.go(Direction.SOUTHWEST)),
            new DirectionButton(button, arrowSouthEast, topMiddleX + 0.67f * unitWidth, topMiddleY - 3 * unitHeight, unitWidth, unitHeight, sea -> sea.go(Direction.SOUTHEAST))
        );

        Gdx.input.setInputProcessor(new InputHandler());
    }

    public void draw() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        uiViewport.apply();
        uiBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        uiBatch.begin();
        uiBatch.draw(uiPanelBackgroundTexture, 75 * gridSquare, 0, 25 * gridSquare, 100 * gridSquare);
        cursive.write(uiBatch, 80 * gridSquare, screenHeight - 2 * gridSquare, " Pirates! @ ^_^");

        directionButtons.forEach(db -> db.draw(uiBatch));

        uiBatch.end();
    }

    public void resize(int width, int height) {
        uiViewport.update(width, height, true);
    }

    public void dispose() {
        uiPanelBackgroundTexture.dispose();
        uiBatch.dispose();
    }

    static class DirectionButton {
        private final Texture button;
        private final Texture arrow;
        private final float worldX;
        private final float worldY;
        private final float widthInWorld;
        private final float heightInWorld;
        private final Consumer<Sea> action;
        private final Rectangle rectangle;

        DirectionButton(Texture button, Texture arrow, float worldX, float worldY, float widthInWorld, float heightInWorld, Consumer<Sea> action) {
            this.button = button;
            this.arrow = arrow;
            this.worldX = worldX;
            this.worldY = worldY;
            this.widthInWorld = widthInWorld;
            this.heightInWorld = heightInWorld;
            this.action = action;

            this.rectangle = new Rectangle(worldX, worldY, widthInWorld, heightInWorld);
        }

        void draw(SpriteBatch batch) {
            batch.draw(button, worldX, worldY, widthInWorld, heightInWorld);
            batch.draw(arrow, worldX, worldY, widthInWorld, heightInWorld);
        }

        boolean pointInside(Vector2 screenPoint) {
            return this.rectangle.contains(screenPoint);
        }

        void actOn(Sea sea) {
            this.action.accept(sea);
        }
    }

    class InputHandler implements InputProcessor {

        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            return false;
        }

        @Override
        public boolean keyTyped(char character) {
            return false;
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                return directionButtons.stream()
                    .filter(db -> db.pointInside(uiViewport.unproject(new Vector2(screenX, screenY))))
                    .findFirst()
                    .map(db -> {
                        db.actOn(sea);
                        return true;
                    }).orElse(false);
            }
            return false;
        }

        @Override
        public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
            return false;
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return false;
        }

        @Override
        public boolean scrolled(float amountX, float amountY) {
            return false;
        }
    }
}
