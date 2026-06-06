package dev.wycor.pirates.game;

public class Monster extends Combatant {
    protected Monster(String name, int health, int attack, int defence) {
        super(name, health, attack, defence);
    }

    @Override
    Attack receiveAttack(Attack attack) {
        this.health = Math.max(0, this.health - attack.actualDamage());
        return attack;
    }
}
