package rush.game;

import rush.domain.Dish;
import rush.domain.Order;
import rush.domain.OrderStatus;

/** A copy of order state. The menu dishes cannot be changed. */
public final class OrderView {
    private final int id;
    private final int customerId;
    private final Dish meal;
    private final long billCents;
    private final int placedTurn;
    private final int completedUnits;
    private final OrderStatus status;

    public OrderView(int id, int customerId, Dish meal, long billCents, int placedTurn, int completedUnits, OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.meal = meal;
        this.billCents = billCents;
        this.placedTurn = placedTurn;
        this.completedUnits = completedUnits;
        this.status = status;
    }

    public int id() { return id; }
    public int customerId() { return customerId; }
    public Dish meal() { return meal; }
    public long billCents() { return billCents; }
    public int placedTurn() { return placedTurn; }
    public int completedUnits() { return completedUnits; }
    public OrderStatus status() { return status; }

    static OrderView from(Order order) {
        return new OrderView(order.id(), order.customer().id(), order.meal(), order.billCents(),
            order.placedTurn(), order.completedUnits(), order.status());
    }
}
