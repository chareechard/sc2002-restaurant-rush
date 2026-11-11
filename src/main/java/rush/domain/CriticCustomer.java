package rush.domain;

/** Critics always assess the Pasta, unlike the Regular customer's rotating preference. */
public final class CriticCustomer extends Customer {
    public CriticCustomer(int id, int arrivalTurn) { super(id, arrivalTurn); }
    @Override public String type() { return "Critic"; }
    @Override public int waitingLoss() { return 12; }
    @Override public Dish chooseMeal(Menu menu) { return menu.named("Pasta"); }
}
