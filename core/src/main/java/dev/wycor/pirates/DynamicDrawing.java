package dev.wycor.pirates;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class DynamicDrawing {
    public static Texture createSolidTexture(float red, float green, float blue, float alpha) {
        return createSolidTexture(1, 1, red, green, blue, alpha);
    }

    public static Texture createSolidTexture(int width, int height, float red, float green, float blue, float alpha) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(red, green, blue, alpha);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
