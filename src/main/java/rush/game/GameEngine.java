package rush.game;

import java.util.ArrayList;
import java.util.List;
import rush.domain.Customer;
import rush.domain.CustomerStatus;
import rush.domain.Menu;
import rush.domain.Money;
import rush.domain.Order;
import rush.domain.Receipt;
import rush.domain.RuleViolation;
import rush.domain.Table;
import rush.staff.Cashier;
import rush.staff.Chef;
import rush.staff.Waiter;

/** Runs each turn. Customers, orders and tables handle their own rules. */
public final class GameEngine {
    private final Scenario scenario = new Scenario();
    private final Menu menu = new Menu();
    private final Waiter waiter = new Waiter();
    private final Chef chef = new Chef();
    private final Cashier cashier = new Cashier();
    private final List<Customer> customers = new ArrayList<Customer>();
    private final List<Table> tables = new ArrayList<Table>();
    private final List<Order> orders = new ArrayList<Order>();
    private final List<Receipt> receipts = new ArrayList<Receipt>();
    private final List<String> events = new ArrayList<String>();
    private int turn;
    private int servedCount;
    private int unhappyDepartures;
    private TurnPhase phase = TurnPhase.BETWEEN_TURNS;

    public GameEngine() {
        for (int id = 1; id <= Scenario.TABLES; id++) {
            tables.add(new Table(id));
        }
    }

    public int turn() { return turn; }
    public TurnPhase phase() { return phase; }
    public Menu menu() { return menu; }
    public List<Customer> customers() {
        List<Customer> copy = new ArrayList<Customer>();
        for (Customer customer : customers) {
            copy.add(customer);
        }
        return copy;
    }

    public List<Customer> waitingQueue() {
        List<Customer> waiting = new ArrayList<Customer>();
        for (Customer customer : customers) {
            if (customer.status() == CustomerStatus.WAITING) {
                waiting.add(customer);
            }
        }
        return waiting;
    }

    public List<Table> tables() {
        List<Table> copy = new ArrayList<Table>();
        for (Table table : tables) {
            copy.add(table);
        }
        return copy;
    }
    public List<Order> orders() {
        List<Order> copy = new ArrayList<Order>();
        for (Order order : orders) {
            copy.add(order);
        }
        return copy;
    }
    public List<Receipt> receipts() {
        List<Receipt> copy = new ArrayList<Receipt>();
        for (Receipt receipt : receipts) {
            copy.add(receipt);
        }
        return copy;
    }
    public List<String> events() {
        List<String> copy = new ArrayList<String>();
        for (String event : events) {
            copy.add(event);
        }
        return copy;
    }

    public Customer customer(int id) {
        for (Customer customer : customers) {
            if (customer.id() == id) {
                return customer;
            }
        }
        throw new RuleViolation("Unknown customer C" + id + ".");
    }

    public Order order(int id) {
        for (Order order : orders) {
            if (order.id() == id) {
                return order;
            }
        }
        throw new RuleViolation("Unknown order O" + id + ".");
    }

    public Table table(int id) {
        for (Table table : tables) {
            if (table.id() == id) {
                return table;
            }
        }
        throw new RuleViolation("Unknown table T" + id + ".");
    }

    public void beginTurn() {
        requirePhase(TurnPhase.BETWEEN_TURNS);
        turn++;
        events.clear();
        if (scenario.hasArrival(turn)) {
            Customer arrival = scenario.arrival(turn);
            customers.add(arrival);
            events.add("C" + arrival.id() + " arrives (" + arrival.type() + ", satisfaction 100).");
        }
        phase = TurnPhase.WAITER;
    }

    public void seatByWaiter(int customerId, int tableId) {
        requirePhase(TurnPhase.WAITER);
        waiter.seat(customer(customerId), table(tableId));
        events.add("Waiter seats C" + customerId + " at T" + tableId + ".");
        phase = TurnPhase.CHEF;
    }

    public void takeOrder(int customerId) {
        requirePhase(TurnPhase.WAITER);
        Order order = waiter.takeOrder(customer(customerId), menu, turn);
        orders.add(order);
        events.add("C" + customerId + " chooses " + order.meal().name() + "; O" + order.id()
            + " bill locked at " + Money.format(order.billCents()) + ".");
        phase = TurnPhase.CHEF;
    }

    public void serve(int orderId) {
        requirePhase(TurnPhase.WAITER);
        waiter.serve(order(orderId), turn);
        servedCount++;
        events.add("Waiter serves O" + orderId + ": SERVED. Customer begins eating.");
        phase = TurnPhase.CHEF;
    }

    public void waiterWait() {
        requirePhase(TurnPhase.WAITER);
        phase = TurnPhase.CHEF;
    }

    public void chefWait() {
        requirePhase(TurnPhase.CHEF);
        phase = TurnPhase.END;
    }

    public void prepare(int orderId) {
        requirePhase(TurnPhase.CHEF);
        Order order = order(orderId);
        chef.prepare(order, turn);
        events.add("Chef advances O" + orderId + " to " + order.completedUnits() + "/"
            + order.meal().preparationUnits() + " units: " + order.status() + ".");
        phase = TurnPhase.END;
    }

    public void finishTurn() {
        requirePhase(TurnPhase.END);

        // A customer finishes eating one turn after being served.
        for (Customer customer : customers) {
            CustomerStatus before = customer.status();
            customer.finishEating(turn);
            if (before == CustomerStatus.EATING && customer.status() == CustomerStatus.READY_TO_PAY) {
                events.add("C" + customer.id() + " finishes eating: READY_TO_PAY; earliest payment next turn.");
            }
        }
        // Collect at most one payment, after a full turn of being ready to pay.
        Receipt receipt = cashier.collect(customers, tables, turn);
        if (receipt != null) {
            receipts.add(receipt);
            events.add("Cashier collects " + Money.format(receipt.billCents()) + " from C"
                + receipt.customerId() + ": PAID; table freed.");
        }

        // Customers who have not been served lose patience and may leave.
        for (Customer customer : customers) {
            if (customer.losePatience()) {
                unhappyDepartures++;
                for (Table table : tables) {
                    if (table.occupant() == customer) {
                        table.release();
                        break;
                    }
                }
                events.add("C" + customer.id() + " leaves unhappy; any unserved order is CANCELLED.");
            }
        }
        phase = turn == Scenario.MAX_TURNS ? TurnPhase.FINISHED : TurnPhase.BETWEEN_TURNS;
    }

    public GameStats stats() {
        long revenue = 0;
        int satisfactionTotal = 0;
        for (Receipt receipt : receipts) {
            revenue += receipt.billCents();
            satisfactionTotal += receipt.satisfaction();
        }

        double average = 0;
        if (!receipts.isEmpty()) {
            average = (double) satisfactionTotal / receipts.size();
        }

        GameResult result = GameResult.IN_PROGRESS;
        if (phase == TurnPhase.FINISHED) {
            if (receipts.size() >= Scenario.MIN_PAID
                    && revenue >= Scenario.MIN_REVENUE_CENTS
                    && average >= Scenario.MIN_SATISFACTION) {
                result = GameResult.VICTORY;
            } else {
                result = GameResult.DEFEAT;
            }
        }
        return new GameStats(turn, servedCount, receipts.size(), unhappyDepartures, revenue, average, result);
    }

    private void requirePhase(TurnPhase expected) {
        RuleViolation.require(phase == expected, "This task is unavailable in phase " + phase
            + "; expected " + expected + ". A staff task cannot be used twice.");
    }
}
