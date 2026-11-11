package rush.domain;

public final class Order {
    private final int id;
    private final Customer customer;
    private final Dish meal;
    private final long billCents;
    private final int placedTurn;
    private int completedUnits;
    private int readyTurn;
    private OrderStatus status = OrderStatus.PREPARING;

    public Order(int id, Customer customer, Dish meal, long billCents, int placedTurn) {
        RuleViolation.require(id > 0 && customer != null && meal != null && billCents >= 0 && placedTurn > 0,
            "Invalid order.");
        this.id = id;
        this.customer = customer;
        this.meal = meal;
        this.billCents = billCents;
        this.placedTurn = placedTurn;
    }

    public int id() { return id; }
    public Customer customer() { return customer; }
    public Dish meal() { return meal; }
    public long billCents() { return billCents; }
    public int placedTurn() { return placedTurn; }
    public int completedUnits() { return completedUnits; }
    public OrderStatus status() { return status; }

    public void prepare(int turn) {
        RuleViolation.require(status == OrderStatus.PREPARING, "Chef can only advance a PREPARING order.");
        completedUnits++;
        if (completedUnits == meal.preparationUnits()) {
            status = OrderStatus.READY;
            readyTurn = turn;
        }
    }

    public void serve(int turn) {
        RuleViolation.require(status == OrderStatus.READY && readyTurn < turn,
            "Dish must have been READY before this turn's Waiter task.");
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        RuleViolation.require(status == OrderStatus.SERVED, "Only a served order can be paid.");
        status = OrderStatus.PAID;
    }

    public void cancel() {
        RuleViolation.require(status == OrderStatus.PREPARING || status == OrderStatus.READY,
            "Only an unserved order can be cancelled.");
        status = OrderStatus.CANCELLED;
    }
}
