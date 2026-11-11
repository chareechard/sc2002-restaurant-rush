package rush.game;

import rush.domain.Table;

/** A copy of table occupancy; a null customer id represents a free table. */
public final class TableView {
    private final int id;
    private final Integer customerId;

    public TableView(int id, Integer customerId) {
        this.id = id;
        this.customerId = customerId;
    }

    public int id() { return id; }
    public Integer customerId() { return customerId; }

    public boolean isFree() { return customerId == null; }

    static TableView from(Table table) {
        return new TableView(table.id(), table.isFree() ? null : table.occupant().id());
    }
}
