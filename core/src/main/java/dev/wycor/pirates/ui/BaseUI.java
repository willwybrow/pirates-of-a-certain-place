package dev.wycor.pirates.ui;

import com.badlogic.gdx.graphics.Texture;
import dev.wycor.pirates.game.Treasure;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class BaseUI {
    public static final float SIXTEEN_PIXELS = 0.08f;
    static final float CURSIVE_LETTER_HEIGHT = SIXTEEN_PIXELS * 9f / 32f;
    static final float CURSIVE_LETTER_WIDTH = SIXTEEN_PIXELS / 4f;
    public static final float THIRTY_TWO_PIXELS = 0.16f;

    private static final EnumMap<Treasure, Texture> TREASURE_TEXTURES = new EnumMap<>(Treasure.class);
    private static boolean treasureTexturesLoaded;

    public static synchronized void loadTreasureTextures() {
        if (treasureTexturesLoaded) {
            return;
        }

        TREASURE_TEXTURES.put(Treasure.EMERALD_OF_HOPE, new Texture("emerald_of_hope_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.GOLDEN_SWORD_OF_YR, new Texture("golden_sword_of_yr_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.KING_FLYNNS_ROYAL_SCEPTRE, new Texture("king_flynns_royal_sceptre_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.SACRED_ONYX_CROSS, new Texture("sacred_onyx_cross_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.LOST_PEARL_OF_JEHVA, new Texture("lost_pearl_of_jehva_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.QUEEN_LATHAS_CROWN, new Texture("queen_lathas_crown_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.RUBY_RING_OF_POWER, new Texture("ruby_ring_of_power_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.SILVER_CHALICE_OF_AUNGE, new Texture("silver_chalice_of_aunge_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.MURPHYS_CHEST_OF_GOLD, new Texture("murphys_chest_of_gold_1x_32.png"));
        TREASURE_TEXTURES.put(Treasure.QUEEN_LATHAS_NECKLACE, new Texture("queen_lathas_necklace_1x_32.png"));

        treasureTexturesLoaded = true;
    }

    public static Texture treasureTexture(Treasure treasure) {
        loadTreasureTextures();
        return TREASURE_TEXTURES.get(treasure);
    }

    public static Map<Treasure, Texture> treasureTextures() {
        loadTreasureTextures();
        return Collections.unmodifiableMap(TREASURE_TEXTURES);
    }

    public static synchronized void disposeTreasureTextures() {
        if (!treasureTexturesLoaded) {
            return;
        }

        TREASURE_TEXTURES.values().forEach(Texture::dispose);
        TREASURE_TEXTURES.clear();
        treasureTexturesLoaded = false;
    }
}
