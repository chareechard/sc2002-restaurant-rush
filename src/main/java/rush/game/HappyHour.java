package rush.game;

import rush.domain.Money;
import rush.domain.PricingPolicy;
import rush.domain.RuleViolation;

/** A one-shot, two-turn pricing policy. Engine controls its turn-start lifecycle. */
public final class HappyHour implements PricingPolicy {
    private int activatedTurn;
    private int currentTurn;

    public void startTurn(int turn, boolean activate) {
        RuleViolation.require(turn == currentTurn + 1, "Happy Hour must advance exactly one turn.");
        RuleViolation.require(!activate || !used(), "Happy Hour has already been activated this game.");
        currentTurn = turn;
        if (activate) { activatedTurn = turn; }
    }
    public boolean used() { return activatedTurn > 0; }
    public int remainingTurns() { return used() ? Math.max(0, activatedTurn + 2 - currentTurn) : 0; }
    public boolean active() { return remainingTurns() > 0; }
    @Override public long apply(long customerDiscountedCents) {
        return active() ? Money.percentage(customerDiscountedCents, 80) : customerDiscountedCents;
    }
}
