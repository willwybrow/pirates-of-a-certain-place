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
import dev.wycor.pirates.game.Game;
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
    private List<ActionButton> weaponButtons;
    private ActionButton fleeButton;
    private ActionButton newGameButton;
    private final Game game;
    private final java.util.function.LongSupplier clock;

    public DrawableUI(Game game, float worldWidth, float worldHeight, float unitWidth, float unitHeight) {
        this(game, worldWidth, worldHeight, unitWidth, unitHeight, System::currentTimeMillis);
    }

    public DrawableUI(Game game, float worldWidth, float worldHeight, float unitWidth, float unitHeight,
                      java.util.function.LongSupplier clock) {
        this.game = game;
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
        float movementControlsCenterY = worldHeight * 0.77f;

        float verticalSpacing = 0.78f;
        float horizontalSpacing = 0.53f;

        this.directionButtons = List.of(
            new DirectionButton(button, arrowNorthWest, uiCentreX - horizontalSpacing * unitWidth, movementControlsCenterY + verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.NORTHWEST, now())),
            new DirectionButton(button, arrowNorthEast, uiCentreX + horizontalSpacing * unitWidth, movementControlsCenterY + verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.NORTHEAST, now())),
            new DirectionButton(button, arrowWest, uiCentreX - 2f * horizontalSpacing * unitWidth, movementControlsCenterY, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.WEST, now())),
            /* new DirectionButton(button, button, uiCentreX, movementControlsCenterY - 2 * unitHeight, unitWidth, unitHeight, sea -> sea.whatsAt(sea.currentPosition()).complete()), // TODO -- middle button?? */
            new DirectionButton(button, arrowEast, uiCentreX + 2f * horizontalSpacing * unitWidth, movementControlsCenterY, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.EAST, now())),
            new DirectionButton(button, arrowSouthWest, uiCentreX - horizontalSpacing * unitWidth, movementControlsCenterY - verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.SOUTHWEST, now())),
            new DirectionButton(button, arrowSouthEast, uiCentreX + horizontalSpacing * unitWidth, movementControlsCenterY - verticalSpacing * unitHeight, unitWidth, unitHeight, sea -> sea.attemptToTravel(Direction.SOUTHEAST, now()))
        );

        float weaponBottomY = ACTION_BUTTON_HEIGHT;
        float lowestDirectionButtonY = movementControlsCenterY - (verticalSpacing * unitHeight);
        float lowestDirectionBottom = lowestDirectionButtonY - (unitHeight / 2f);
        float weaponTopLimit = lowestDirectionBottom - ACTION_BUTTON_HEIGHT;

        float defaultWeaponButtonSpacing = ACTION_BUTTON_HEIGHT * 1.15f;
        float weaponButtonSpacing = defaultWeaponButtonSpacing;
        List<Weapon> weapons = game.playerDetails().wieldableWeapons();
        if (weapons.size() > 1) {
            float maxSpacingToAvoidOverlap = (weaponTopLimit - weaponBottomY) / (weapons.size() - 1);
            weaponButtonSpacing = Math.min(defaultWeaponButtonSpacing, maxSpacingToAvoidOverlap);
            weaponButtonSpacing = Math.max(0f, weaponButtonSpacing);
        }

        float firstWeaponY = weaponBottomY;

        ArrayList<ActionButton> weaponButtons = new ArrayList<>(weapons.size());
        for (int i = 0; i < weapons.size(); i++) {
            Weapon weapon = weapons.get(weapons.size() - 1 - i);
            float buttonY = firstWeaponY + (i * weaponButtonSpacing);
            Texture weaponTexture = BaseUI.weaponTexture(weapon);
            weaponButtons.add(new ActionButton(
                buttonRectUp,
                buttonRectDown,
                uiCentreX,
                buttonY,
                ACTION_BUTTON_WIDTH,
                ACTION_BUTTON_HEIGHT,
                weaponTexture,
                sea -> weaponButtonLabel(sea, weapon),
                sea -> sea.attemptToAttack(weapon, now()),
                true,
                true
            ));
        }
        this.weaponButtons = weaponButtons;

        this.fleeButton = new ActionButton(
            buttonRectUp,
            buttonRectDown,
            uiCentreX,
            movementControlsCenterY,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT,
            "Flee",
            sea -> sea.attemptToFlee(now())
        );

        this.newGameButton = new ActionButton(
            buttonRectUp,
            buttonRectDown,
            uiCentreX,
            movementControlsCenterY,
            ACTION_BUTTON_WIDTH,
            ACTION_BUTTON_HEIGHT,
            "New Game",
            sea -> sea.startNewGame(now())
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

        boolean activeCombat = game.hasActiveCombat();

        if (game.isGameOver()) {
            newGameButton.draw(uiBatch, cursive, game);
        } else if (activeCombat) {
            cursive.write(uiBatch, 4 * gridSquare, worldHeight - 8 * gridSquare, "COMBAT!");
            fleeButton.draw(uiBatch, cursive, game);
        } else {
            directionButtons.forEach(db -> db.draw(uiBatch));
        }

        weaponButtons.forEach(button -> button.draw(uiBatch, cursive, game, activeCombat));

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

    private static String weaponButtonLabel(Game game, Weapon weapon) {
        String label = weaponButtonLabel(weapon);
        if (!weapon.usesAmmunition()) {
            return label;
        }

        int ammunition = game.playerDetails().ammunitionByWeapon().getOrDefault(weapon, 0);
        return label + " [" + ammunition + "]";
    }

    private static String weaponButtonLabel(Weapon weapon) {
        return weapon.displayName();
    }

    static class DirectionButton {
        private final Texture button;
        private final Texture arrow;
        private final Consumer<Game> action;
        private final Rectangle rectangle;

        DirectionButton(Texture button, Texture arrow, float worldX, float worldY, float widthInWorld, float heightInWorld, Consumer<Game> action) {
            this.button = button;
            this.arrow = arrow;
            this.action = action;

            this.rectangle = new Rectangle(worldX - widthInWorld / 2f, worldY - heightInWorld / 2f, widthInWorld, heightInWorld);
        }

        void draw(SpriteBatch batch) {
            batch.draw(button, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
            batch.draw(arrow, rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        }

        boolean pointInside(Vector2 screenPoint) {
            return this.rectangle.contains(screenPoint);
        }

        void actOn(Game game) {
            this.action.accept(game);
        }
    }

    static class ActionButton {
        private final Texture upTexture;
        private final Texture downTexture;
        private final Texture iconTexture;
        private final Function<Game, String> label;
        private final Consumer<Game> action;
        private final boolean leftAlignedContent;
        private final boolean hideBackgroundWhenDisabled;
        private final Rectangle rectangle;
        private boolean pressed;

        ActionButton(Texture upTexture, Texture downTexture, float worldCenterX, float worldCenterY, float widthInWorld,
                      float heightInWorld, String label, Consumer<Game> action) {
            this(
                upTexture,
                downTexture,
                worldCenterX,
                worldCenterY,
                widthInWorld,
                heightInWorld,
                null,
                sea -> label,
                action,
                false,
                false
            );
        }

        ActionButton(Texture upTexture, Texture downTexture, float worldCenterX, float worldCenterY, float widthInWorld,
                     float heightInWorld, Texture iconTexture, Function<Game, String> label, Consumer<Game> action) {
            this(upTexture, downTexture, worldCenterX, worldCenterY, widthInWorld, heightInWorld,
                iconTexture, label, action, false, false);
        }

        ActionButton(Texture upTexture, Texture downTexture, float worldCenterX, float worldCenterY, float widthInWorld,
                     float heightInWorld, Texture iconTexture, Function<Game, String> label, Consumer<Game> action,
                     boolean leftAlignedContent, boolean hideBackgroundWhenDisabled) {
            this.upTexture = upTexture;
            this.downTexture = downTexture;
            this.iconTexture = iconTexture;
            this.action = action;
            this.label = label;
            this.leftAlignedContent = leftAlignedContent;
            this.hideBackgroundWhenDisabled = hideBackgroundWhenDisabled;
            this.rectangle = new Rectangle(
                worldCenterX - widthInWorld / 2f,
                worldCenterY - heightInWorld / 2f,
                widthInWorld,
                heightInWorld
            );
        }

        void draw(SpriteBatch batch, Cursive cursive, Game game) {
            draw(batch, cursive, game, true);
        }

        void draw(SpriteBatch batch, Cursive cursive, Game game, boolean enabled) {
            if (enabled || !hideBackgroundWhenDisabled) {
                batch.draw(enabled && pressed ? downTexture : upTexture, rectangle.x, rectangle.y, rectangle.width,
                    rectangle.height);
            }

            String text = label.apply(game);
            float textWidth = text.length() * BaseUI.CURSIVE_LETTER_WIDTH;

            float iconWidth = 0f;
            float iconGap = 0f;
            if (iconTexture != null) {
                float iconHeight = rectangle.height * 0.8f;
                iconWidth = iconHeight * ((float) iconTexture.getWidth() / (float) iconTexture.getHeight());
                iconGap = BaseUI.CURSIVE_LETTER_WIDTH / 2f;
            }

            float contentWidth = textWidth + iconWidth + iconGap;
            float contentX;
            if (leftAlignedContent) {
                contentX = rectangle.x + (BaseUI.CURSIVE_LETTER_WIDTH * 0.8f);
            } else {
                contentX = rectangle.x + (rectangle.width - contentWidth) / 2f;
            }

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

        void actOn(Game game) {
            this.action.accept(game);
        }
    }

    class InputHandler implements InputProcessor {

        @Override
        public boolean keyDown(int keycode) {
            return false;
        }

        @Override
        public boolean keyUp(int keycode) {
            if (game.isGameOver()) {
                switch (keycode) {
                    case Input.Keys.ENTER:
                    case Input.Keys.N:
                        game.startNewGame(now());
                        return true;
                    default:
                        return false;
                }
            }

            if (game.hasActiveCombat()) {
                switch (keycode) {
                    case Input.Keys.SPACE:
                        game.attemptToAttack(Weapon.CUTLASS, now());
                        return true;
                    case Input.Keys.F:
                        game.attemptToFlee(now());
                        return true;
                    default:
                        return false;
                }
            }

            switch(keycode) {
                case Input.Keys.E:
                    game.attemptToTravel(Direction.NORTHEAST, now());
                    return true;
                case Input.Keys.D:
                    game.attemptToTravel(Direction.EAST, now());
                    return true;
                case Input.Keys.X:
                    game.attemptToTravel(Direction.SOUTHEAST, now());
                    return true;
                case Input.Keys.Z:
                    game.attemptToTravel(Direction.SOUTHWEST, now());
                    return true;
                case Input.Keys.A:
                    game.attemptToTravel(Direction.WEST, now());
                    return true;
                case Input.Keys.W:
                    game.attemptToTravel(Direction.NORTHWEST, now());
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
            if (button == Input.Buttons.LEFT && game.isGameOver()) {
                boolean inside = newGameButton.pointInside(clickPoint);
                newGameButton.setPressed(inside);
                return inside;
            }

            if (button == Input.Buttons.LEFT && game.hasActiveCombat()) {
                boolean anyPressed = false;
                for (ActionButton weaponButton : weaponButtons) {
                    boolean inside = weaponButton.pointInside(clickPoint);
                    weaponButton.setPressed(inside);
                    anyPressed = anyPressed || inside;
                }

                boolean fleePressed = fleeButton.pointInside(clickPoint);
                fleeButton.setPressed(fleePressed);
                anyPressed = anyPressed || fleePressed;

                return anyPressed;
            }

            return false;
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
                Vector2 clickPoint = uiViewport.unproject(new Vector2(screenX, screenY));

                if (game.isGameOver()) {
                    boolean acted = newGameButton.pointInside(clickPoint);
                    if (acted) {
                        newGameButton.actOn(game);
                    }
                    newGameButton.setPressed(false);
                    return acted;
                }

                if (game.hasActiveCombat()) {
                    boolean acted = weaponButtons.stream()
                        .filter(b -> b.pointInside(clickPoint))
                        .findFirst()
                        .map(b -> {
                            b.actOn(game);
                            return true;
                        })
                        .orElse(false);

                    if (!acted && fleeButton.pointInside(clickPoint)) {
                        fleeButton.actOn(game);
                        acted = true;
                    }

                    weaponButtons.forEach(b -> b.setPressed(false));
                    fleeButton.setPressed(false);
                    return acted;
                }

                return directionButtons.stream()
                    .filter(db -> db.pointInside(clickPoint))
                    .findFirst()
                    .map(db -> {
                        db.actOn(game);
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
