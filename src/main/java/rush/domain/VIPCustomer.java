package rush.domain;

public final class VIPCustomer extends Customer {
    public VIPCustomer(int id, int arrivalTurn) { super(id, arrivalTurn); }
    @Override public String type() { return "VIP"; }
    @Override public int waitingLoss() { return 5; }
    @Override public MenuItem chooseMeal(Menu menu) { return menu.named("Burger"); }
    @Override public long discountedBill(long baseCents) { return Money.percentage(baseCents, 90); }
}
