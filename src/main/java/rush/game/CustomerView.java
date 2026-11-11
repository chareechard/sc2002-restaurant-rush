package rush.game;

import rush.domain.Customer;
import rush.domain.CustomerStatus;

/** A copy of customer state with no operations that can change the game. */
public final class CustomerView {
    private final int id;
    private final String type;
    private final int satisfaction;
    private final CustomerStatus status;
    private final int readyToPayTurn;

    public CustomerView(int id, String type, int satisfaction, CustomerStatus status, int readyToPayTurn) {
        this.id = id;
        this.type = type;
        this.satisfaction = satisfaction;
        this.status = status;
        this.readyToPayTurn = readyToPayTurn;
    }

    public int id() { return id; }
    public String type() { return type; }
    public int satisfaction() { return satisfaction; }
    public CustomerStatus status() { return status; }
    public int readyToPayTurn() { return readyToPayTurn; }

    static CustomerView from(Customer customer) {
        return new CustomerView(customer.id(), customer.type(), customer.satisfaction(),
            customer.status(), customer.readyToPayTurn());
    }
}
