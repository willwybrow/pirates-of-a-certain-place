package dev.wycor.pirates;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.wycor.pirates.game.AttackResolver;
import dev.wycor.pirates.game.Game;
import dev.wycor.pirates.game.GameRandom;
import dev.wycor.pirates.game.HazardEngine;
import dev.wycor.pirates.game.TileFactory;
import dev.wycor.pirates.ui.BaseUI;
import dev.wycor.pirates.ui.DrawableStatus;
import dev.wycor.pirates.ui.DrawableUI;
import dev.wycor.pirates.ui.DrawableWorld;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private static final float SCREEN_WIDTH = 1.6f;
    private static final float SCREEN_HEIGHT = 1.0f;
    private static final float LEFT_WIDTH = SCREEN_WIDTH * (2f / 3f);
    private static final float RIGHT_WIDTH = SCREEN_WIDTH - LEFT_WIDTH;
    private static final float TOP_LEFT_HEIGHT = SCREEN_HEIGHT * (2f / 3f);
    private static final float BOTTOM_LEFT_HEIGHT = SCREEN_HEIGHT - TOP_LEFT_HEIGHT;
    private static final float HEX_WIDTH = BaseUI.THIRTY_TWO_PIXELS;
    private static final float HEX_HEIGHT = BaseUI.THIRTY_TWO_PIXELS;
    private static final float TARGET_ASPECT = SCREEN_WIDTH / SCREEN_HEIGHT;

    private final Game game;
    private final Random seedSource;

    private final DrawableWorld world;
    private final DrawableStatus status;
    private final DrawableUI ui;

    public Main() {
        this.seedSource = new Random();
        GameRandom gameRandom = new GameRandom(seedSource.nextLong());
        TileFactory tileFactory = new TileFactory(gameRandom.world());
        AttackResolver attackResolver = new AttackResolver(gameRandom.combat());
        HazardEngine hazardEngine = new HazardEngine(gameRandom.hazard());
        this.game = new Game(tileFactory, attackResolver, hazardEngine);

        world = new DrawableWorld(game, LEFT_WIDTH, TOP_LEFT_HEIGHT, HEX_WIDTH, HEX_HEIGHT);
        status = new DrawableStatus(game, LEFT_WIDTH, BOTTOM_LEFT_HEIGHT);
        ui = new DrawableUI(game, RIGHT_WIDTH, SCREEN_HEIGHT, HEX_WIDTH, HEX_HEIGHT);
    }

    @Override
    public void create() {
        world.create();
        status.create();
        ui.create();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        ScreenUtils.clear(0.08f, 0.25f, 0.3625f, 1f);

        world.draw(dt);
        status.draw();
        ui.draw();
    }


    @Override
    public void resize(int width, int height) {
        int fittedWidth = width;
        int fittedHeight = Math.round(width / TARGET_ASPECT);

        if (fittedHeight > height) {
            fittedHeight = height;
            fittedWidth = Math.round(height * TARGET_ASPECT);
        }

        int fittedX = (width - fittedWidth) / 2;
        int fittedY = (height - fittedHeight) / 2;

        world.resize(fittedX, fittedY, fittedWidth, fittedHeight);
        status.resize(fittedX, fittedY, fittedWidth, fittedHeight);
        ui.resize(fittedX, fittedY, fittedWidth, fittedHeight);
    }

    @Override
    public void dispose() {
        world.dispose();
        status.dispose();
        ui.dispose();
    }
}
