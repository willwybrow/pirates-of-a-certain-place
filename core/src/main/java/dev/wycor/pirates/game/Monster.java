package dev.wycor.pirates.game;

public class Monster extends Combatant {
    protected Monster(String name, int health, int attack, int defence) {
        super(name, health, attack, defence);
    }

    @Override
    Attack receiveAttackFrom(Combatant combatant) {
        Attack attackReceived = new Attack(combatant, this, combatant.attack, this.defence);
        this.health = Math.max(0, this.health - attackReceived.actualDamage());
        return attackReceived;
    }
}
