package rush.staff;

import rush.domain.Customer;
import rush.domain.Menu;
import rush.domain.Order;
import rush.domain.PricingPolicy;
import rush.domain.Table;

public final class Waiter {
    public void seat(Customer customer, Table table) { table.seat(customer); }
    public Order takeOrder(Customer customer, Menu menu, PricingPolicy pricing, int turn) {
        return customer.placeOrder(menu, pricing, turn);
    }
    public void serve(Order order, int turn) { order.customer().serve(turn); }
}
