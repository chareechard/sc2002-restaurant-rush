package rush.domain;

/** Stores the bill and satisfaction when a customer pays. */
public final class Receipt {
    private final int customerId;
    private final long billCents;
    private final int satisfaction;
    private final int paidTurn;

    public Receipt(int customerId, long billCents, int satisfaction, int paidTurn) {
        this.customerId = customerId;
        this.billCents = billCents;
        this.satisfaction = satisfaction;
        this.paidTurn = paidTurn;
    }

    public int customerId() { return customerId; }
    public long billCents() { return billCents; }
    public int satisfaction() { return satisfaction; }
    public int paidTurn() { return paidTurn; }
}
