package rush.staff;

import rush.domain.Customer;
import rush.domain.Menu;
import rush.domain.Order;
import rush.domain.Table;

public final class Waiter {
    public void seat(Customer customer, Table table) { table.seat(customer); }
    public Order takeOrder(Customer customer, Menu menu, int turn) { return customer.placeOrder(menu, turn); }
    public void serve(Order order, int turn) { order.customer().serve(turn); }
}
