package rush.domain;

public final class RegularCustomer extends Customer {
    public RegularCustomer(int id, int arrivalTurn) { super(id, arrivalTurn); }
    @Override public String type() { return "Regular"; }
    @Override public int waitingLoss() { return 8; }
    @Override public MenuItem chooseMeal(Menu menu) {
        switch (id() % 3) {
            case 1:
                return menu.named("Pasta");
            case 2:
                return menu.named("Burger");
            default:
                return menu.named("Salad");
        }
    }
}
