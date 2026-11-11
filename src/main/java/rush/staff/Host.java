package rush.staff;

import rush.domain.Customer;
import rush.domain.Table;

public final class Host {
    public void seat(Customer customer, Table table) { table.seat(customer); }
}
