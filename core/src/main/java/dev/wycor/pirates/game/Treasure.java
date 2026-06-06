package dev.wycor.pirates.game;

public enum Treasure {
    EMERALD_OF_HOPE("Emerald of Hope"),
    GOLDEN_SWORD_OF_YR("Golden Sword of Yr"),
    KING_FLYNNS_ROYAL_SCEPTRE("King Flynn's Royal Sceptre"),
    SACRED_ONYX_CROSS("Sacred Onyx Cross"),
    LOST_PEARL_OF_JEHVA("Lost Pearl of Jehva"),
    QUEEN_LATHAS_CROWN("Queen Latha's Crown"),
    RUBY_RING_OF_POWER("Ruby Ring of Power"),
    SILVER_CHALICE_OF_AUNGE("Silver Chalice of Aunge"),
    MURPHYS_CHEST_OF_GOLD("Murphy's Chest of Gold"),
    QUEEN_LATHAS_NECKLACE("Queen Latha's Necklace");

    private final String displayName;

    Treasure(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return this.displayName;
    }
}
