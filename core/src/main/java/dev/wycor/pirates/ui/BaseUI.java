package dev.wycor.pirates.ui;

import com.badlogic.gdx.graphics.Texture;
import dev.wycor.pirates.game.Treasure;
import dev.wycor.pirates.game.Weapon;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class BaseUI {
    public static final float SIXTEEN_PIXELS = 0.08f;
    static final float CURSIVE_LETTER_HEIGHT = SIXTEEN_PIXELS * 9f / 32f;
    static final float CURSIVE_LETTER_WIDTH = SIXTEEN_PIXELS / 4f;
    public static final float THIRTY_TWO_PIXELS = 0.16f;

    private static final EnumMap<Treasure, Texture> TREASURE_TEXTURES = new EnumMap<>(Treasure.class);
    private static final EnumMap<Weapon, Texture> WEAPON_TEXTURES = new EnumMap<>(Weapon.class);
    private static boolean treasureTexturesLoaded;
    private static boolean weaponTexturesLoaded;

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

    public static synchronized void loadWeaponTextures() {
        if (weaponTexturesLoaded) {
            return;
        }

        WEAPON_TEXTURES.put(Weapon.CUTLASS, new Texture("swords_1x_24.png"));
        WEAPON_TEXTURES.put(Weapon.CANNON, new Texture("cannons_1x_24.png"));
        WEAPON_TEXTURES.put(Weapon.GIANT_AXE, new Texture("giant_axe_1x_24.png"));
        WEAPON_TEXTURES.put(Weapon.FLAMING_ARROWS, new Texture("flaming_arrows_1x_24.png"));
        WEAPON_TEXTURES.put(Weapon.HARPOON, new Texture("harpoon_1x_24.png"));
        WEAPON_TEXTURES.put(Weapon.ICE_DAGGERS, new Texture("ice_daggers_1x_24.png"));

        weaponTexturesLoaded = true;
    }

    public static Texture treasureTexture(Treasure treasure) {
        loadTreasureTextures();
        return TREASURE_TEXTURES.get(treasure);
    }

    public static Map<Treasure, Texture> treasureTextures() {
        loadTreasureTextures();
        return Collections.unmodifiableMap(TREASURE_TEXTURES);
    }

    public static Texture weaponTexture(Weapon weapon) {
        loadWeaponTextures();
        return WEAPON_TEXTURES.get(weapon);
    }

    public static Map<Weapon, Texture> weaponTextures() {
        loadWeaponTextures();
        return Collections.unmodifiableMap(WEAPON_TEXTURES);
    }

    public static synchronized void disposeTreasureTextures() {
        if (!treasureTexturesLoaded) {
            return;
        }

        TREASURE_TEXTURES.values().forEach(Texture::dispose);
        TREASURE_TEXTURES.clear();
        treasureTexturesLoaded = false;
    }

    public static synchronized void disposeWeaponTextures() {
        if (!weaponTexturesLoaded) {
            return;
        }

        WEAPON_TEXTURES.values().forEach(Texture::dispose);
        WEAPON_TEXTURES.clear();
        weaponTexturesLoaded = false;
    }
}
