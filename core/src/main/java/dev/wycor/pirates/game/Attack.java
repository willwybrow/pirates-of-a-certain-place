package dev.wycor.pirates.game;

public class Attack {
    private final Combatant initiator;
    private final Combatant defender;
    private final Weapon weapon;
    private int actualDamage;

    public Attack(Combatant initiator, Combatant defender, Weapon weapon) {
        this.initiator = initiator;
        this.defender = defender;
        this.weapon = weapon;
        this.actualDamage = Math.max(0, weapon.baseDamage());
    }

    public int actualDamage() {
        return this.actualDamage;
    }

    public void setActualDamage(int actualDamage) {
        this.actualDamage = Math.max(0, actualDamage);
    }

    public int baseAttackDamage() {
        return this.weapon.baseDamage();
    }

    public Combatant initiator() {
        return initiator;
    }

    public Combatant defender() {
        return defender;
    }

    public Weapon weapon() {
        return weapon;
    }
}
