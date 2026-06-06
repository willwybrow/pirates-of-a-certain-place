package dev.wycor.pirates;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ScreenUtils;
import dev.wycor.pirates.game.Sea;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public static final float SIXTEEN_PIXELS = 0.08f;
    public static final float THIRTY_TWO_PIXELS = 0.16f;
    private static final float SCREEN_WIDTH = 1.6f;
    private static final float SCREEN_HEIGHT = 1.0f;
    private static final float LEFT_WIDTH = SCREEN_WIDTH * (2f / 3f);
    private static final float RIGHT_WIDTH = SCREEN_WIDTH - LEFT_WIDTH;
    private static final float TOP_LEFT_HEIGHT = SCREEN_HEIGHT * (2f / 3f);
    private static final float BOTTOM_LEFT_HEIGHT = SCREEN_HEIGHT - TOP_LEFT_HEIGHT;
    private static final float HEX_WIDTH = THIRTY_TWO_PIXELS;
    private static final float HEX_HEIGHT = THIRTY_TWO_PIXELS;
    private static final float TARGET_ASPECT = SCREEN_WIDTH / SCREEN_HEIGHT;

    private final Sea sea = new Sea();
    private final DrawableWorld world = new DrawableWorld(sea, LEFT_WIDTH, TOP_LEFT_HEIGHT, HEX_WIDTH, HEX_HEIGHT);
    private final DrawableStatus status = new DrawableStatus(sea, LEFT_WIDTH, BOTTOM_LEFT_HEIGHT);
    private final DrawableUI ui = new DrawableUI(sea, RIGHT_WIDTH, SCREEN_HEIGHT, HEX_WIDTH, HEX_HEIGHT);

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
