package rush.domain;

/** Owns the customer lifecycle. Subclasses provide preferences, patience and pricing. */
public abstract class Customer {
    private final int id;
    private final int arrivalTurn;
    private int satisfaction = 100;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;

    protected Customer(int id, int arrivalTurn) {
        RuleViolation.require(id > 0 && arrivalTurn > 0, "Invalid customer identity.");
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    public abstract String type();
    public abstract int waitingLoss();
    public abstract Dish chooseMeal(Menu menu);
    public long discountedBill(long baseCents) { return baseCents; }

    public int id() { return id; }
    public int arrivalTurn() { return arrivalTurn; }
    public int satisfaction() { return satisfaction; }
    public CustomerStatus status() { return status; }
    public Order order() { return order; }
    public int readyToPayTurn() { return readyToPayTurn; }

    public boolean isWaitingForService() {
        return status == CustomerStatus.WAITING || status == CustomerStatus.SEATED
            || status == CustomerStatus.WAITING_FOR_FOOD;
    }

    public void seat() {
        RuleViolation.require(status == CustomerStatus.WAITING, "Customer is not in the waiting queue.");
        status = CustomerStatus.SEATED;
    }

    public Order placeOrder(Menu menu, int turn) {
        return placeOrder(menu, PricingPolicy.STANDARD, turn);
    }

    public Order placeOrder(Menu menu, PricingPolicy pricing, int turn) {
        RuleViolation.require(status == CustomerStatus.SEATED, "Customer must be seated and have no order.");
        Dish meal = chooseMeal(menu);
        order = new Order(id, this, meal, pricing.apply(discountedBill(meal.priceCents())), turn);
        status = CustomerStatus.WAITING_FOR_FOOD;
        return order;
    }

    public void serve(int turn) {
        RuleViolation.require(status == CustomerStatus.WAITING_FOR_FOOD, "Customer is not waiting for food.");
        order.serve(turn);
        servedTurn = turn;
        status = CustomerStatus.EATING;
    }

    public void finishEating(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
        }
    }

    public Receipt pay(int turn) {
        RuleViolation.require(status == CustomerStatus.READY_TO_PAY && readyToPayTurn < turn,
            "Customer must have been ready to pay on an earlier turn.");
        order.markPaid();
        status = CustomerStatus.PAID;
        return new Receipt(id, order.billCents(), satisfaction, turn);
    }

    /** @return true only when this call causes an unhappy departure. */
    public boolean losePatience() {
        if (!isWaitingForService()) {
            return false;
        }
        satisfaction = Math.max(0, satisfaction - waitingLoss());
        if (satisfaction > 0) {
            return false;
        }
        if (order != null) {
            order.cancel();
        }
        status = CustomerStatus.LEFT;
        return true;
    }

    public void recoverSatisfaction(int points) {
        RuleViolation.require(points >= 0, "Recovery cannot be negative.");
        if (isWaitingForService()) { satisfaction = (int) Math.min(100L, (long) satisfaction + points); }
    }
}
