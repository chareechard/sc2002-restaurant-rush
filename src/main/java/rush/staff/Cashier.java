package rush.staff;

import java.util.List;
import rush.domain.Customer;
import rush.domain.CustomerStatus;
import rush.domain.Receipt;
import rush.domain.RuleViolation;
import rush.domain.Table;

public final class Cashier {
    /** Returns the receipt, or null when no customer can pay this turn. */
    public Receipt collect(List<Customer> customers, List<Table> tables, int turn) {
        Customer next = null;
        for (Customer customer : customers) {
            if (customer.status() != CustomerStatus.READY_TO_PAY || customer.readyToPayTurn() >= turn) {
                continue;
            }

            // Oldest ready customer first; use the smaller id to break a tie.
            if (next == null || customer.readyToPayTurn() < next.readyToPayTurn()
                    || (customer.readyToPayTurn() == next.readyToPayTurn() && customer.id() < next.id())) {
                next = customer;
            }
        }

        if (next == null) {
            return null;
        }

        for (Table table : tables) {
            if (table.occupant() == next) {
                Receipt receipt = next.pay(turn);
                table.release();
                return receipt;
            }
        }
        throw new RuleViolation("Paying customer has no table.");
    }
}
