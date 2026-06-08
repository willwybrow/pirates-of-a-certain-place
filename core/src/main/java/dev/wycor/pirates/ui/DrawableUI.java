package dev.wycor.pirates.ui;

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
import dev.wycor.pirates.game.Weapon;
import dev.wycor.pirates.geometry.Direction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.wycor.pirates.ui.DynamicDrawing.createSolidTexture;

public class DrawableUI {
    private static final float PIXELS_TO_WORLD = 0.005f;
    private static final float ACTION_BUTTON_WIDTH = 100f * PIXELS_TO_WORLD;
    private static final float ACTION_BUTTON_HEIGHT = 15f * PIXELS_TO_WORLD;

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
    private Texture buttonRectUp;
    private Texture buttonRectDown;

    private final Cursive cursive;

    private Viewport uiViewport;
    private SpriteBatch uiBatch;

    private List<DirectionButton> directionButtons;
    private List<ActionButton> combatButtons;
    private List<ActionButton> gameOverButtons;
    private final Sea sea;
    private final java.util.function.LongSupplier clock;

    public DrawableUI(Sea sea, float worldWidth, float worldHeight, float unitWidth, float unitHeight) {
        this(sea, worldWidth, worldHeight, unitWidth, unitHeight, System::currentTimeMillis);
    }

    public DrawableUI(Sea sea, float worldWidth, float worldHeight, float unitWidth, float unitHeight,
                      java.util.function.LongSupplier clock) {
        this.sea = sea;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        this.gridSquare = worldWidth / 100f;
        this.unitWidth = unitWidth;
        this.unitHeight = unitHeight;
        this.cursive = new Cursive();
        this.clock = clock;
    }

    private long now() {
        return clock.getAsLong();
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
        buttonRectUp = new Texture("button_rect_up_72x15.png");
        buttonRectDown = new Texture("button_rect_down_72x15.png");
        BaseUI.loadWeaponTextures();

        cursive.create();

        uiViewport = new FitViewport(worldWidth, worldHeight);
        uiBatch = new SpriteBatch();

        // the seven movement buttons

        float uiCentreX = worldWidth / 2f;
        float uiCentreY = worldHeight / 2f;

        float verticalSpacing = 0.85f;
        float horizontalSpacing = 0.53f;

        this.directionButtons = List.of(
            new DirectionButton(button, arrowNorthWest, uiCentreX - horizontalSpacing * unitWidth, uiCentreY + verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.NORTHWEST, now())),
            new DirectionButton(button, arrowNorthEast, uiCentreX + horizontalSpacing * unitWidth, uiCentreY + verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.NORTHEAST, now())),
            new DirectionButton(button, arrowWest, uiCentreX - 2f * horizontalSpacing * unitWidth, uiCentreY, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.WEST, now())),
            /* new DirectionButton(button, button, uiCentreX, uiCentreY - 2 * unitHeight, unitWidth, unitHeight, sea -> sea.whatsAt(sea.currentPosition()).complete()), // TODO -- middle button?? */
            new DirectionButton(button, arrowEast, uiCentreX + 2f * horizontalSpacing * unitWidth, uiCentreY, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.EAST, now())),
            new DirectionButton(button, arrowSouthWest, uiCentreX - horizontalSpacing * unitWidth, uiCentreY - verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.SOUTHWEST, now())),
            new DirectionButton(button, arrowSouthEast, uiCentreX + horizontalSpacing * unitWidth, uiCentreY - verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.SOUTHEAST, now()))
        );

        float combatButtonsY = worldHeight * 0.5f;
        float combatButtonSpacing = ACTION_BUTTON_HEIGHT * 1.15f;
        List<Weapon> weapons = sea.playerDetails().wieldableWeapons();
        float firstWeaponY = combatButtonsY + combatButtonSpacing * ((weapons.size() - 1) / 2f);

        ArrayList<ActionButton> combatButtons = new ArrayList<>(weapons.size() + 1);
        for (int i = 0; i < weapons.size(); i++) {
            Weapon weapon = weapons.get(i);
            float buttonY = firstWeaponY - (i * combatButtonSpacing);
            Texture weaponTexture = BaseUI.weaponTexture(weapon);
            combatButtons.add(new ActionButton(buttonRectUp, buttonRectDown, uiCentreX, buttonY,
                ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT, weaponTexture, sea -> weaponButtonLabel(sea, weapon),
                sea -> sea.attemptToAttack(weapon, now())));
        }

        combatButtons.add(new ActionButton(buttonRectUp, buttonRectDown, uiCentreX,
            firstWeaponY - (weapons.size() * combatButtonSpacing),
            ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT, "Flee", sea -> sea.attemptToFlee(now())));

        this.combatButtons = combatButtons;

        this.gameOverButtons = List.of(
            new ActionButton(buttonRectUp, buttonRectDown, uiCentreX, worldHeight * 0.45f,
                ACTION_BUTTON_WIDTH, ACTION_BUTTON_HEIGHT, "New Game", sea -> sea.startNewGame(now()))
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

        if (sea.isGameOver()) {
            gameOverButtons.forEach(button -> button.draw(uiBatch, cursive, sea));
        } else if (sea.hasActiveCombat()) {
            cursive.write(uiBatch, 4 * gridSquare, worldHeight - 8 * gridSquare, "COMBAT!");
            combatButtons.forEach(button -> button.draw(uiBatch, cursive, sea));
        } else {
            directionButtons.forEach(db -> db.draw(uiBatch));
        }

        uiBatch.end();
    }

    public void resize(int screenX, int screenY, int screenWidth, int screenHeight) {
        int leftWidth = Math.round(screenWidth * (2f / 3f));
        int rightWidth = screenWidth - leftWidth;

        uiViewport.update(rightWidth, screenHeight, true);
        uiViewport.setScreenBounds(screenX + leftWidth, screenY, rightWidth, screenHeight);
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
        buttonRectUp.dispose();
        buttonRectDown.dispose();
        BaseUI.disposeWeaponTextures();
        uiBatch.dispose();
    }

    private static String weaponButtonLabel(Sea sea, Weapon weapon) {
        String label = weaponButtonLabel(weapon);
        if (!weapon.usesAmmunition()) {
            return label;
        }

        int ammunition = sea.playerDetails().ammunitionByWeapon().getOrDefault(weapon, 0);
        return label + " [" + ammunition + "]";
    }

    private static String weaponButtonLabel(Weapon weapon) {
        return weapon.displayName();
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

    static class ActionButton {
        private final Texture upTexture;
        private final Texture downTexture;
        private final Texture iconTexture;
        private final Function<Sea, String> label;
        private final Consumer<Sea> action;
        private final Rectangle rectangle;
        private boolean pressed;

        ActionButton(Texture upTexture, Texture downTexture, float worldCenterX, float worldCenterY, float widthInWorld,
                      float heightInWorld, String label, Consumer<Sea> action) {
            this(upTexture, downTexture, worldCenterX, worldCenterY, widthInWorld, heightInWorld, null, sea -> label, action);
        }

        ActionButton(Texture upTexture, Texture downTexture, float worldCenterX, float worldCenterY, float widthInWorld,
                     float heightInWorld, Texture iconTexture, Function<Sea, String> label, Consumer<Sea> action) {
            this.upTexture = upTexture;
            this.downTexture = downTexture;
            this.iconTexture = iconTexture;
            this.action = action;
            this.label = label;
            this.rectangle = new Rectangle(
                worldCenterX - widthInWorld / 2f,
                worldCenterY - heightInWorld / 2f,
                widthInWorld,
                heightInWorld
            );
        }

        void draw(SpriteBatch batch, Cursive cursive, Sea sea) {
            batch.draw(pressed ? downTexture : upTexture, rectangle.x, rectangle.y, rectangle.width, rectangle.height);

            String text = label.apply(sea);
            float textWidth = text.length() * BaseUI.CURSIVE_LETTER_WIDTH;

            float iconWidth = 0f;
            float iconGap = 0f;
            if (iconTexture != null) {
                float iconHeight = rectangle.height * 0.8f;
                iconWidth = iconHeight * ((float) iconTexture.getWidth() / (float) iconTexture.getHeight());
                iconGap = BaseUI.CURSIVE_LETTER_WIDTH / 2f;
            }

            float contentWidth = textWidth + iconWidth + iconGap;
            float contentX = rectangle.x + (rectangle.width - contentWidth) / 2f;

            if (iconTexture != null) {
                float iconHeight = rectangle.height * 0.8f;
                float iconY = rectangle.y + (rectangle.height - iconHeight) / 2f;
                batch.draw(iconTexture, contentX, iconY, iconWidth, iconHeight);
            }

            float textX = contentX + iconWidth + iconGap;
            float textY = rectangle.y + (rectangle.height - BaseUI.CURSIVE_LETTER_HEIGHT) / 2f;
            cursive.write(batch, textX, textY, text);
        }

        boolean pointInside(Vector2 screenPoint) {
            return this.rectangle.contains(screenPoint);
        }

        void setPressed(boolean pressed) {
            this.pressed = pressed;
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
            if (sea.isGameOver()) {
                switch (keycode) {
                    case Input.Keys.ENTER:
                    case Input.Keys.N:
                        sea.startNewGame(now());
                        return true;
                    default:
                        return false;
                }
            }

            if (sea.hasActiveCombat()) {
                switch (keycode) {
                    case Input.Keys.SPACE:
                        sea.attemptToAttack(Weapon.CUTLASS, now());
                        return true;
                    case Input.Keys.F:
                        sea.attemptToFlee(now());
                        return true;
                    default:
                        return false;
                }
            }

            switch(keycode) {
                case Input.Keys.E:
                    sea.attemptToTravel(Direction.NORTHEAST, now());
                    return true;
                case Input.Keys.D:
                    sea.attemptToTravel(Direction.EAST, now());
                    return true;
                case Input.Keys.X:
                    sea.attemptToTravel(Direction.SOUTHEAST, now());
                    return true;
                case Input.Keys.Z:
                    sea.attemptToTravel(Direction.SOUTHWEST, now());
                    return true;
                case Input.Keys.A:
                    sea.attemptToTravel(Direction.WEST, now());
                    return true;
                case Input.Keys.W:
                    sea.attemptToTravel(Direction.NORTHWEST, now());
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
            Vector2 clickPoint = uiViewport.unproject(new Vector2(screenX, screenY));
            if (button == Input.Buttons.LEFT && sea.isGameOver()) {
                boolean anyPressed = false;
                for (ActionButton gameOverButton : gameOverButtons) {
                    boolean inside = gameOverButton.pointInside(clickPoint);
                    gameOverButton.setPressed(inside);
                    anyPressed = anyPressed || inside;
                }
                return anyPressed;
            }

            if (button == Input.Buttons.LEFT && sea.hasActiveCombat()) {
                boolean anyPressed = false;
                for (ActionButton combatButton : combatButtons) {
                    boolean inside = combatButton.pointInside(clickPoint);
                    combatButton.setPressed(inside);
                    anyPressed = anyPressed || inside;
                }
                return anyPressed;
            }

            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                Vector2 clickPoint = uiViewport.unproject(new Vector2(screenX, screenY));

                if (sea.isGameOver()) {
                    boolean acted = gameOverButtons.stream()
                        .filter(b -> b.pointInside(clickPoint))
                        .findFirst()
                        .map(b -> {
                            b.actOn(sea);
                            return true;
                        })
                        .orElse(false);
                    gameOverButtons.forEach(b -> b.setPressed(false));
                    return acted;
                }

                if (sea.hasActiveCombat()) {
                    boolean acted = combatButtons.stream()
                        .filter(b -> b.pointInside(clickPoint))
                        .findFirst()
                        .map(b -> {
                            b.actOn(sea);
                            return true;
                        })
                        .orElse(false);
                    combatButtons.forEach(b -> b.setPressed(false));
                    return acted;
                }

                return directionButtons.stream()
                    .filter(db -> db.pointInside(clickPoint))
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
