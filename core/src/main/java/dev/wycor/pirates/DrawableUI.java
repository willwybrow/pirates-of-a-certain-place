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
    private final float worldWidth;
    private final float worldHeight;
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

    private List<DirectionButton> directionButtons;
    private final Sea sea;

    public DrawableUI(Sea sea, float worldWidth, float worldHeight, float unitWidth, float unitHeight) {
        this.sea = sea;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        this.gridSquare = worldWidth / 100f;
        this.unitWidth = unitWidth;
        this.unitHeight = unitHeight;
        this.cursive = new Cursive();
    }

    public void create() {
        uiPanelBackgroundTexture = createSolidTexture(0f, 0f, 0f, 1f);
        button = new Texture("button_up_16.png");
        arrowNorthEast = new Texture("arrow_upright_16.png");
        arrowEast = new Texture("arrow_right_16.png");
        arrowSouthEast = new Texture("arrow_downright_16.png");
        arrowNorthWest = new Texture("arrow_upleft_16.png");
        arrowWest = new Texture("arrow_left_16.png");
        arrowSouthWest = new Texture("arrow_downleft_16.png");

        cursive.create();

        uiViewport = new FitViewport(worldWidth, worldHeight);
        uiBatch = new SpriteBatch();

        // the seven movement buttons

        float uiCentreX = worldWidth / 2f;
        float uiCentreY = worldHeight / 2f;

        float verticalSpacing = 0.85f;
        float horizontalSpacing = 0.53f;

        this.directionButtons = List.of(
            new DirectionButton(button, arrowNorthWest, uiCentreX - horizontalSpacing * unitWidth, uiCentreY + verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.NORTHWEST)),
            new DirectionButton(button, arrowNorthEast, uiCentreX + horizontalSpacing * unitWidth, uiCentreY + verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.NORTHEAST)),
            new DirectionButton(button, arrowWest, uiCentreX - 2f * horizontalSpacing * unitWidth, uiCentreY, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.WEST)),
            /* new DirectionButton(button, button, uiCentreX, uiCentreY - 2 * unitHeight, unitWidth, unitHeight, sea -> sea.whatsAt(sea.currentPosition()).complete()), // TODO -- middle button?? */
            new DirectionButton(button, arrowEast, uiCentreX + 2f * horizontalSpacing * unitWidth, uiCentreY, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.EAST)),
            new DirectionButton(button, arrowSouthWest, uiCentreX - horizontalSpacing * unitWidth, uiCentreY - verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.SOUTHWEST)),
            new DirectionButton(button, arrowSouthEast, uiCentreX + horizontalSpacing * unitWidth, uiCentreY - verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.SOUTHEAST))
        );

        Gdx.input.setInputProcessor(new InputHandler());
    }

    public void draw() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);

        uiViewport.apply();
        uiBatch.setProjectionMatrix(uiViewport.getCamera().combined);

        uiBatch.begin();
        uiBatch.draw(uiPanelBackgroundTexture, 0f, 0f, worldWidth, worldHeight);
        cursive.write(uiBatch, 4 * gridSquare, worldHeight - 3 * gridSquare, " Pirates! @ ^_^");

        directionButtons.forEach(db -> db.draw(uiBatch));

        uiBatch.end();
    }

    public void resize(int width, int height) {
        int leftWidth = Math.round(width * (2f / 3f));
        int rightWidth = width - leftWidth;

        uiViewport.update(rightWidth, height, true);
        uiViewport.setScreenBounds(leftWidth, 0, rightWidth, height);
    }

    public void dispose() {
        uiPanelBackgroundTexture.dispose();
        button.dispose();
        arrowNorthEast.dispose();
        arrowEast.dispose();
        arrowSouthEast.dispose();
        arrowNorthWest.dispose();
        arrowWest.dispose();
        arrowSouthWest.dispose();
        uiBatch.dispose();
    }

    static class DirectionButton {
        private final Texture button;
        private final Texture arrow;
        private final Consumer<Sea> action;
        private final Rectangle rectangle;

        DirectionButton(Texture button, Texture arrow, float worldX, float worldY, float widthInWorld, float heightInWorld, Consumer<Sea> action) {
            this.button = button;
            this.arrow = arrow;
            this.action = action;

            this.rectangle = new Rectangle(worldX - widthInWorld / 2f, worldY + heightInWorld / 2f, widthInWorld, heightInWorld);
        }

        void draw(SpriteBatch batch) {
            batch.draw(button, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            batch.draw(arrow, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
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
            switch(keycode) {
                case Input.Keys.E:
                    sea.attemptToTravel(Direction.NORTHEAST);
                    return true;
                case Input.Keys.D:
                    sea.attemptToTravel(Direction.EAST);
                    return true;
                case Input.Keys.X:
                    sea.attemptToTravel(Direction.SOUTHEAST);
                    return true;
                case Input.Keys.Z:
                    sea.attemptToTravel(Direction.SOUTHWEST);
                    return true;
                case Input.Keys.A:
                    sea.attemptToTravel(Direction.WEST);
                    return true;
                case Input.Keys.W:
                    sea.attemptToTravel(Direction.NORTHWEST);
                    return true;
            }
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
