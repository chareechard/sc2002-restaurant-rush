package rush.domain;

public final class Table {
    private final int id;
    private Customer occupant;

    public Table(int id) {
        RuleViolation.require(id > 0, "Invalid table id.");
        this.id = id;
    }

    public int id() { return id; }
    public Customer occupant() { return occupant; }
    public boolean isFree() { return occupant == null; }

    public void seat(Customer customer) {
        RuleViolation.require(isFree(), "Table " + id + " is occupied.");
        RuleViolation.require(customer != null, "A customer is required.");
        customer.seat();
        occupant = customer;
    }

    public void release() {
        RuleViolation.require(occupant != null && (occupant.status() == CustomerStatus.PAID
            || occupant.status() == CustomerStatus.LEFT), "Table stays occupied until payment or departure.");
        occupant = null;
    }
}
