package dev.wycor.pirates.game;

public class Attack{
    private final Combatant initiator;
    private final Combatant defender;
    private final int unmitigatedAttackDamage;
    private final int defenderMitigation;

    public Attack(Combatant initiator, Combatant defender, int unmitigatedAttackDamage, int defenderMitigation) {
        this.initiator = initiator;
        this.defender = defender;
        this.unmitigatedAttackDamage = unmitigatedAttackDamage;
        this.defenderMitigation = defenderMitigation;
    }

    public int actualDamage() {
        return Math.max(0, unmitigatedAttackDamage - defenderMitigation);
    }

    public Combatant initiator() {
        return initiator;
    }

    public Combatant defender() {
        return defender;
    }
}
