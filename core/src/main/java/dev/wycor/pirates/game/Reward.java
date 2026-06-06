package dev.wycor.pirates.game;

public class Reward {
    private static final Reward NONE = new Reward(0, 0);

    private final int health;
    private final int food;

    public Reward(int health, int food) {
        this.health = health;
        this.food = food;
    }

    public static Reward none() {
        return NONE;
    }

    public static Reward food(int amount) {
        return new Reward(0, amount);
    }

    public static Reward health(int amount) {
        return new Reward(amount, 0);
    }

    public int health() {
        return health;
    }

    public int food() {
        return food;
    }

    public boolean hasAny() {
        return health != 0 || food != 0;
    }
}
