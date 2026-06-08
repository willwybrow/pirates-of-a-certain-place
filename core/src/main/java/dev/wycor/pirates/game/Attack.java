package dev.wycor.pirates.game;

public class Attack {
    private final Combatant initiator;
    private final Combatant defender;
    private final Weapon weapon;
    private final int unmitigatedAttackDamage;
    private final int defenderMitigation;
    private int actualDamage;

    public Attack(Combatant initiator, Combatant defender, Weapon weapon, int unmitigatedAttackDamage, int defenderMitigation) {
        this.initiator = initiator;
        this.defender = defender;
        this.weapon = weapon;
        this.unmitigatedAttackDamage = unmitigatedAttackDamage;
        this.defenderMitigation = defenderMitigation;
        this.actualDamage = Math.max(0, unmitigatedAttackDamage - defenderMitigation);
    }

    public int actualDamage() {
        return this.actualDamage;
    }

    public void setActualDamage(int actualDamage) {
        this.actualDamage = Math.max(0, actualDamage);
    }

    public int unmitigatedAttackDamage() {
        return this.unmitigatedAttackDamage;
    }

    public int defenderMitigation() {
        return this.defenderMitigation;
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
