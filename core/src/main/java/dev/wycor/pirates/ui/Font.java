package dev.wycor.pirates.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;

import static dev.wycor.pirates.DynamicDrawing.createSolidTexture;

public class Font {

    private final float size;
    private static final char[] KEYS = {
        '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.',        '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        ':', ';', '<', '=', '>', '?', '@',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '[', '\\', ']', '^', '_', '`',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    Texture fontSheet;
    Texture space;
    private final HashMap<Character, TextureRegion> codePage = new HashMap<>();

    public Font(float size) {
        this.size = size;
    }

    public void create() {
        fontSheet = new Texture("font/Cursive_transparent_sheet.png");
        space = createSolidTexture(1f, 1f, 1f, 0f);
        TextureRegion[][] textureRegions = TextureRegion.split(fontSheet, 9, 9);
        for (int i = 0; i < KEYS.length; i++) {
           codePage.put(KEYS[i], textureRegions[0][i]);
        }
        codePage.put(' ', new TextureRegion(space));
    }

    public void write(SpriteBatch batch, float x, float y, String words) {
        var letters = words.toCharArray();

        for (int i = 0; i < letters.length; i++) {
            batch.draw(codePage.get(letters[i]), x + ((float)i) * size, y, size, size);
        }
    }
}
