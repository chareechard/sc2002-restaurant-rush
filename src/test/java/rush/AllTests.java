package rush;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import rush.cli.ConsoleApp;
import rush.domain.Customer;
import rush.domain.CriticCustomer;
import rush.domain.ComboMeal;
import rush.domain.MenuItem;
import rush.domain.PricingPolicy;
import rush.domain.CustomerStatus;
import rush.domain.Menu;
import rush.domain.Money;
import rush.domain.Order;
import rush.domain.OrderStatus;
import rush.domain.Receipt;
import rush.domain.RegularCustomer;
import rush.domain.RuleViolation;
import rush.domain.Table;
import rush.domain.VIPCustomer;
import rush.game.GameEngine;
import rush.game.GameResult;
import rush.game.CustomerView;
import rush.game.OrderView;
import rush.game.TableView;
import rush.game.HappyHour;
import rush.game.Scenario;
import rush.game.TurnPhase;
import rush.staff.Cashier;

/** No external framework: assertions throw even when -ea was accidentally omitted. */
public final class AllTests {
    private static int checks;
    private AllTests() { }

    public static void main(String[] args) throws IOException {
        pastaTimeline();
        invalidActionsAreAtomic();
        abandonmentAndRevenue();
        cashierOrderAndLimit();
        independentDomains();
        domainTimingGuards();
        engineSnapshots();
        victoryBoundaries();
        customerPolymorphism();
        hostAndCombo();
        happyHourPolicy();
        happyHourGameFlow();
        setupValidation();
        fullRuns();
        meaningfulDecision();
        cliInput();
        bomReplays();
        cliHelpAndWhitespace();
        System.out.println("PASS: " + checks + " checks (Stage 3)");
    }

    private static void pastaTimeline() {
        GameEngine g = new GameEngine();
        begin(g); g.seatByWaiter(1, 1); end(g);
        eq(92, g.customer(1).satisfaction(), "turn 1 satisfaction");
        begin(g); g.takeOrder(1); g.prepare(1); g.finishTurn();
        eq(1, g.order(1).completedUnits(), "new order can be prepared immediately");
        eq(84, g.customer(1).satisfaction(), "turn 2 satisfaction");
        begin(g);
        try {
            g.serve(1);
            throw new AssertionError("Expected invalid action: cannot serve unfinished food");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(TurnPhase.WAITER, g.phase(), "invalid serve retains Waiter");
        g.waiterWait(); g.prepare(1);
        eq(TurnPhase.END, g.phase(), "Waiter is unavailable after same-turn cooking");
        g.finishTurn();
        eq(OrderStatus.READY, g.order(1).status(), "READY after 2 units");
        eq(76, g.customer(1).satisfaction(), "turn 3 satisfaction");
        begin(g); g.serve(1); end(g);
        eq(CustomerStatus.EATING, g.customer(1).status(), "served customer still eating");
        eq(0, g.stats().paidCount(), "cannot pay while eating");
        eq(0L, g.stats().revenueCents(), "served is not revenue");
        begin(g); g.waiterWait(); end(g);
        eq(CustomerStatus.READY_TO_PAY, g.customer(1).status(), "full turn eating");
        eq(0, g.stats().paidCount(), "newly ready customer cannot pay immediately");
        check(!g.table(1).isFree(), "table held until payment");
        begin(g); g.waiterWait(); end(g);
        eq(CustomerStatus.PAID, g.customer(1).status(), "payment turn 6");
        eq(OrderStatus.PAID, g.order(1).status(), "order marked paid");
        eq(1800L, g.stats().revenueCents(), "pasta bill");
        eq(76, g.receipts().get(0).satisfaction(), "record satisfaction at payment");
        g.receipts().clear();
        eq(1, g.receipts().size(), "editing a receipt list copy does not remove payments");
        check(g.table(1).isFree(), "payment frees table");
        eq(GameResult.IN_PROGRESS, g.stats().result(), "no early result");
    }

    private static void invalidActionsAreAtomic() {
        GameEngine g = new GameEngine();
        try {
            g.takeOrder(1);
            throw new AssertionError("Expected invalid action: cannot act before beginning turn");
        } catch (RuleViolation expected) {
            checks++;
        }
        begin(g);
        try {
            g.seatByWaiter(1, 9);
            throw new AssertionError("Expected invalid action: unknown table");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            g.seatByWaiter(99, 1);
            throw new AssertionError("Expected invalid action: unknown customer");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            g.takeOrder(1);
            throw new AssertionError("Expected invalid action: unseated cannot order");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(CustomerStatus.WAITING, g.customer(1).status(), "failed choices keep queue");
        g.seatByWaiter(1, 1);
        try {
            g.takeOrder(1);
            throw new AssertionError("Expected invalid action: one Waiter task");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            g.prepare(99);
            throw new AssertionError("Expected invalid action: unknown order");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(TurnPhase.CHEF, g.phase(), "invalid cook keeps Chef");
        g.chefWait();
        try {
            g.chefWait();
            throw new AssertionError("Expected invalid action: one Chef task");
        } catch (RuleViolation expected) {
            checks++;
        }
        check(!g.table(1).isFree(), "unpaid table is retained by the engine");
        g.finishTurn();
        for (int t = 2; t <= 3; t++) { idle(g); }
        begin(g);
        try {
            g.seatByWaiter(2, 1);
            throw new AssertionError("Expected invalid action: occupied table rejected");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(CustomerStatus.WAITING, g.customer(2).status(), "occupied seat rejection atomic");
        g.seatByWaiter(2, 2); end(g);
        begin(g); g.takeOrder(1); g.prepare(1);
        try {
            g.prepare(1);
            throw new AssertionError("Expected invalid action: one preparation unit per turn");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(1, g.order(1).completedUnits(), "second cook made no progress");
        g.finishTurn();
    }

    private static void abandonmentAndRevenue() {
        GameEngine g = new GameEngine();
        begin(g); g.seatByWaiter(1, 1); end(g);
        begin(g); g.takeOrder(1); end(g);
        while (g.turn() < 13) { idle(g); }
        eq(CustomerStatus.LEFT, g.customer(1).status(), "zero satisfaction leaves");
        eq(0, g.customer(1).satisfaction(), "satisfaction floor");
        eq(OrderStatus.CANCELLED, g.order(1).status(), "unfinished order cancelled");
        check(g.table(1).isFree(), "abandonment frees table");
        eq(0L, g.stats().revenueCents(), "abandoned revenue excluded");
        check(!g.stats().hasAverageSatisfaction(), "no paid average is N/A");
        begin(g); g.waiterWait();
        try {
            g.prepare(1);
            throw new AssertionError("Expected invalid action: cannot cook cancelled order");
        } catch (RuleViolation expected) {
            checks++;
        }
        end(g);
        Customer c = new RegularCustomer(3, 1);
        c.seat(); Order o = c.placeOrder(new Menu(), 1); o.prepare(1);
        for (int i = 0; i < 13; i++) { c.losePatience(); }
        eq(OrderStatus.CANCELLED, o.status(), "ready but unserved order cancelled too");
    }

    private static void cashierOrderAndLimit() {
        // Tables own the customers throughout their service flow.
        Table t1 = new Table(1); Table t2 = new Table(2);
        Customer later = new RegularCustomer(2, 1); Customer earlier = new RegularCustomer(5, 1);
        t1.seat(later); t2.seat(earlier);
        readySeated(later, 3); readySeated(earlier, 2);
        Cashier cashier = new Cashier();
        List<Customer> customers = new ArrayList<Customer>();
        customers.add(later); customers.add(earlier);
        List<Table> tables = new ArrayList<Table>();
        tables.add(t1); tables.add(t2);
        Receipt receipt = cashier.collect(customers, tables, 5);
        if (receipt == null) { throw new AssertionError("An eligible customer should pay"); }
        eq(5, receipt.customerId(), "oldest ready pays first even if listed last");
        eq(CustomerStatus.READY_TO_PAY, later.status(), "only one customer paid");
        check(t2.isFree() && !t1.isFree(), "only payer's table freed");
    }

    private static Customer readyCustomer(int id, int servedTurn) {
        Customer c = new RegularCustomer(id, 1); c.seat(); readySeated(c, servedTurn); return c;
    }

    private static void readySeated(Customer c, int servedTurn) {
        Order o = c.placeOrder(new Menu(), 1);
        for (int i = 0; i < o.meal().preparationUnits(); i++) { o.prepare(1); }
        c.serve(servedTurn); c.finishEating(servedTurn + 1);
    }

    private static void independentDomains() {
        eq(2430L, Money.percentage(2700, 90), "integer percentage");
        eq(1L, Money.percentage(1, 50), "half cents round up");
        eq("$10.80", Money.format(1080), "two decimal money display");
        Scenario scenario = new Scenario();
        int arrivals = 0;
        for (int t = 1; t <= 18; t++) { if (scenario.hasArrival(t)) { arrivals++; } }
        eq(6, arrivals, "exactly six arrivals");
        GameEngine g = new GameEngine();
        g.beginTurn();
        g.customers().clear();
        eq(1, g.customers().size(), "editing a customer list copy does not change the game");
        Customer paid = readyCustomer(2, 2); Receipt receipt = paid.pay(4);
        paid.losePatience();
        eq(receipt.satisfaction(), paid.satisfaction(), "paid satisfaction cannot decay");
    }

    private static void domainTimingGuards() {
        Customer customer = new RegularCustomer(1, 1);
        Table table = new Table(1);
        table.seat(customer);
        Order order = customer.placeOrder(new Menu(), 1);
        order.prepare(1); order.prepare(2);
        try {
            order.serve(2);
            throw new AssertionError("Expected invalid action: domain rejects serving on the ready turn");
        } catch (RuleViolation expected) {
            checks++;
        }
        customer.serve(3);
        try {
            customer.pay(3);
            throw new AssertionError("Expected invalid action: domain rejects payment while eating");
        } catch (RuleViolation expected) {
            checks++;
        }
        customer.finishEating(4);
        try {
            customer.pay(4);
            throw new AssertionError("Expected invalid action: domain rejects payment on readiness turn");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            table.release();
            throw new AssertionError("Expected invalid action: domain rejects releasing an unpaid table");
        } catch (RuleViolation expected) {
            checks++;
        }
        customer.pay(5); table.release();
        check(table.isFree(), "domain releases a paid table");
    }

    private static void engineSnapshots() {
        GameEngine g = new GameEngine();
        g.beginTurn();
        CustomerView queued = g.customer(1);
        TableView free = g.table(1);
        List<CustomerView> originalCustomers = g.customers();
        g.waitingQueue().clear();
        eq(1, g.waitingQueue().size(), "editing a queue copy does not remove waiting customers");
        g.seatByHost(1, 1); g.takeOrder(1);
        OrderView unprepared = g.order(1);
        g.prepare(1); g.finishTurn();
        eq(CustomerStatus.WAITING, queued.status(), "customer snapshot cannot change under its caller");
        eq(100, queued.satisfaction(), "snapshot retains original satisfaction");
        check(free.isFree(), "table snapshot retains original occupancy");
        eq(0, unprepared.completedUnits(), "order snapshot retains original preparation");
        eq(1, g.order(1).completedUnits(), "live state advances only through engine commands");
        eq(CustomerStatus.WAITING_FOR_FOOD, g.customer(1).status(), "fresh customer snapshot reflects live state");
        g.tables().clear();
        eq(2, g.tables().size(), "editing a table list copy does not change the game");
        g.orders().clear();
        eq(1, g.orders().size(), "editing an order list copy does not change the game");
        g.events().clear();
        check(!g.events().isEmpty(), "editing an event list copy does not remove game messages");
        g.menu().items().clear();
        eq(4, g.menu().items().size(), "editing a menu list copy does not remove menu items");
        while (g.turn() < 4) { idle(g); }
        eq(1, originalCustomers.size(), "old customer list does not acquire later arrivals");
        eq(2, g.customers().size(), "fresh customer list includes later arrivals");
    }

    private static void victoryBoundaries() {
        check(Scenario.targetsMet(4, 4500, 240), "all exact victory thresholds are inclusive");
        check(!Scenario.targetsMet(3, 9000, 300), "paid threshold must pass independently");
        check(!Scenario.targetsMet(4, 4499, 400), "one cent below revenue target loses");
        check(!Scenario.targetsMet(4, 9000, 239), "average below sixty loses independently");
        check(Scenario.targetsMet(5, 4500, 300), "satisfaction threshold scales with paid count");
        check(!Scenario.targetsMet(5, 4500, 299), "display rounding cannot rescue a low average");
        check(!Scenario.targetsMet(0, 0, 0), "an empty paid population cannot win");
    }

    private static void bomReplays() throws IOException {
        Scanner source = new Scanner(new File("replays/victory.txt"));
        StringBuilder replayText = new StringBuilder();
        StringBuilder commandsText = new StringBuilder();
        try {
            while (source.hasNextLine()) {
                String line = source.nextLine();
                replayText.append(line).append("\n");
                String trimmed = line.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                    if (commandsText.length() > 0) { commandsText.append("\r\n"); }
                    commandsText.append(line);
                }
            }
        } finally {
            source.close();
        }
        String[] inputs = { "\uFEFF" + replayText.toString(), "\uFEFF" + commandsText.toString() };
        for (String input : inputs) {
            ByteArrayOutputStream transcript = new ByteArrayOutputStream();
            GameEngine g = new ConsoleApp(new Scanner(input), new PrintStream(transcript), true).play();
            check(!transcript.toString().contains("Invalid"), "BOM accepted before a comment or command");
            eq(GameResult.VICTORY, g.stats().result(), "BOM replay preserves victory");
            eq(8610L, g.stats().revenueCents(), "BOM replay preserves revenue");
            eq(84.0, g.stats().averageSatisfaction(), "BOM replay preserves satisfaction");
        }
    }

    private static void cliHelpAndWhitespace() {
        String input = "\n\u2003\n# ignored comment\nHeLp\nStAtUs\nWAIT\nhelp\nstatus\n  SeAt\t1  1  \nOrDeR 1\nCoOk 1\n";
        ByteArrayOutputStream transcript = new ByteArrayOutputStream();
        GameEngine g = new ConsoleApp(new Scanner(input), new PrintStream(transcript), true).play();
        String output = transcript.toString();
        check(!output.contains("Invalid"), "help, mixed case and whitespace are accepted");
        check(output.contains("Host seats C1 at T1."), "help and status do not spend the Host action");
        check(output.contains("C1 chooses Pasta"), "tokenized order command reaches the Waiter");
        check(output.contains("Chef advances O1 to 1/2"), "tokenized cook command reaches the Chef");
        eq(18, g.turn(), "EOF after valid commands completes the game");
    }

    private static void setupValidation() {
        try {
            new RegularCustomer(0, 1);
            throw new AssertionError("Expected invalid action: customer id must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new RegularCustomer(1, 0);
            throw new AssertionError("Expected invalid action: arrival turn must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new Table(0);
            throw new AssertionError("Expected invalid action: table id must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }
        Table table = new Table(1);
        try {
            table.seat(null);
            throw new AssertionError("Expected invalid action: null seating input gives a rule violation");
        } catch (RuleViolation expected) {
            checks++;
        }
        check(table.isFree(), "rejected seating keeps the table free");

        try {
            new MenuItem(null, 900, 1);
            throw new AssertionError("Expected invalid action: menu name cannot be null");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new MenuItem("  ", 900, 1);
            throw new AssertionError("Expected invalid action: menu name cannot be blank");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new MenuItem("Salad", 0, 1);
            throw new AssertionError("Expected invalid action: menu price must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new MenuItem("Salad", 900, 0);
            throw new AssertionError("Expected invalid action: cooking units must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }

        Customer customer = new RegularCustomer(1, 1);
        MenuItem meal = new MenuItem("Salad", 900, 1);
        try {
            new Order(0, customer, meal, 900, 1);
            throw new AssertionError("Expected invalid action: order id must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new Order(1, null, meal, 900, 1);
            throw new AssertionError("Expected invalid action: order needs a customer");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new Order(1, customer, null, 900, 1);
            throw new AssertionError("Expected invalid action: order needs a meal");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new Order(1, customer, meal, -1, 1);
            throw new AssertionError("Expected invalid action: bill cannot be negative");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            new Order(1, customer, meal, 900, 0);
            throw new AssertionError("Expected invalid action: order turn must be positive");
        } catch (RuleViolation expected) {
            checks++;
        }

        try {
            Money.percentage(-1, 50);
            throw new AssertionError("Expected invalid action: amount cannot be negative");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            Money.percentage(900, -1);
            throw new AssertionError("Expected invalid action: percentage cannot be negative");
        } catch (RuleViolation expected) {
            checks++;
        }
        try {
            Money.percentage(900, 101);
            throw new AssertionError("Expected invalid action: percentage cannot exceed 100");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(0L, Money.percentage(0, 50), "zero amount is valid");
        eq(0L, Money.percentage(900, 0), "zero percentage is valid");
        eq(900L, Money.percentage(900, 100), "full percentage is valid");
        try {
            Money.percentage(Long.MAX_VALUE, 100);
            throw new AssertionError("Expected arithmetic failure: multiplication overflow");
        } catch (ArithmeticException expected) {
            checks++;
        }
        try {
            Money.percentage(Long.MAX_VALUE, 1);
            throw new AssertionError("Expected arithmetic failure: rounding addition overflow");
        } catch (ArithmeticException expected) {
            checks++;
        }
    }

    private static void fullRuns() throws IOException {
        GameEngine win = replay("victory");
        eq(GameResult.VICTORY, win.stats().result(), "victory replay");
        eq(18, win.turn(), "victory runs all turns");
        eq(5, win.stats().paidCount(), "five paid");
        eq(8610L, win.stats().revenueCents(), "victory revenue");
        eq(84.0, win.stats().averageSatisfaction(), "victory average");
        eq(6, win.stats().servedCount(), "served and paid are distinct");
        eq(6, win.customers().size(), "six total customers");
        check(win.customer(6).status() != CustomerStatus.PAID, "turn 16 arrival cannot pay by 18");
        try {
            win.beginTurn();
            throw new AssertionError("Expected invalid action: turn 19 prohibited");
        } catch (RuleViolation expected) {
            checks++;
        }
        GameEngine lose = replay("defeat");
        eq(GameResult.DEFEAT, lose.stats().result(), "defeat replay");
        eq(18, lose.turn(), "defeat runs all turns");
        eq(0, lose.stats().paidCount(), "no unearned paid customers");
        eq(2, lose.stats().unhappyDepartures(), "waiting customers abandon");
    }

    private static void customerPolymorphism() {
        Customer vip = new VIPCustomer(2, 4);
        Customer critic = new CriticCustomer(3, 7);
        Customer regular = new RegularCustomer(3, 7);
        vip.losePatience(); critic.losePatience(); regular.losePatience();
        eq(95, vip.satisfaction(), "VIP patience via Customer");
        eq(88, critic.satisfaction(), "Critic patience via Customer");
        eq(92, regular.satisfaction(), "Regular patience unchanged");
        eq(1350L, vip.discountedBill(1500), "VIP ten percent discount");
        eq(1500L, critic.discountedBill(1500), "Critic full price");
        eq("Pasta", critic.chooseMeal(new Menu()).name(), "Critic preference");
        eq("Salad", regular.chooseMeal(new Menu()).name(), "same id Regular differs from Critic");
        eq("VIP", new Scenario().arrival(4).type(), "VIP in base arrivals");
        eq("Critic", new Scenario().arrival(7).type(), "Critic in base arrivals");
    }

    private static void cliInput() throws IOException {
        String input = "nonsense\nwait\nwait\nseat nope 1\nseat 1 3\nseat 1 1 extra\nseat 1 1\ncook 999\nwait\n";
        ByteArrayOutputStream transcript = new ByteArrayOutputStream();
        GameEngine g = new ConsoleApp(new Scanner(input), new PrintStream(transcript), true).play();
        check(transcript.toString().contains("Invalid choice:"), "CLI gives reason");
        eq(18, g.turn(), "EOF does not hang or end early");
        check(transcript.toString().contains("Waiter seats C1"), "valid retry after malformed input");
        check(transcript.toString().contains("Average paid satisfaction=N/A"), "N/A shown");
    }

    private static void meaningfulDecision() throws IOException {
        GameEngine delayed = replay("delayed-vip");
        eq(GameResult.VICTORY, delayed.stats().result(), "one-turn delay still meets targets");
        eq(83.0, delayed.stats().averageSatisfaction(), "one Waiter decision lowers paid average");
        Receipt receipt = null;
        for (Receipt candidate : delayed.receipts()) {
            if (candidate.customerId() == 2) {
                receipt = candidate;
                break;
            }
        }
        if (receipt == null) { throw new AssertionError("Delayed VIP should have paid"); }
        eq(8, receipt.paidTurn(), "delayed service holds table one extra turn");
        eq(95, receipt.satisfaction(), "delayed VIP records lower satisfaction");
        eq(1080L, receipt.billCents(), "service delay does not reprice locked bill");
    }

    private static GameEngine replay(String name) throws IOException {
        Scanner input = new Scanner(new File("replays/" + name + ".txt"));
        ByteArrayOutputStream transcript = new ByteArrayOutputStream();
        try {
            GameEngine game = new ConsoleApp(input, new PrintStream(transcript), true).play();
            check(!transcript.toString().contains("Invalid"), name + " contains only legal choices");
            return game;
        } finally {
            input.close();
        }
    }

    private static void begin(GameEngine g) { g.beginTurn(); g.hostWait(); }
    private static void end(GameEngine g) { g.chefWait(); g.finishTurn(); }
    private static void idle(GameEngine g) { begin(g); g.waiterWait(); end(g); }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static void eq(Object expected, Object actual, String message) {
        check(expected.equals(actual), message + ": expected " + expected + ", got " + actual);
    }

    private static void hostAndCombo() {
        GameEngine g = new GameEngine();
        g.beginTurn();
        try {
            g.seatByHost(1, 9);
            throw new AssertionError("Expected invalid action: invalid Host target retains task");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(TurnPhase.HOST, g.phase(), "Host retry phase");
        g.seatByHost(1, 1);
        try {
            g.seatByHost(1, 2);
            throw new AssertionError("Expected invalid action: Host has only one task");
        } catch (RuleViolation expected) {
            checks++;
        }
        g.takeOrder(1); g.prepare(1); g.finishTurn();
        eq(1, g.order(1).completedUnits(), "Host permits same-turn seat, order and prep");

        GameEngine both = new GameEngine();
        for (int i = 0; i < 3; i++) { idle(both); }
        both.beginTurn(); both.seatByHost(1, 1); both.seatByWaiter(2, 2); end(both);
        check(!both.table(1).isFree() && !both.table(2).isFree(), "Host and Waiter can each seat one customer");

        ComboMeal combo = new ComboMeal(new MenuItem("Salad", 900, 1), new MenuItem("Pasta", 1800, 2));
        eq(2430L, combo.priceCents(), "combo price from components");
        eq(3, combo.preparationUnits(), "combo prep is sum");
        eq(5L, new ComboMeal(new MenuItem("A", 2, 1), new MenuItem("B", 3, 1)).priceCents(), "combo half-cent rounding");
        Customer c = new RegularCustomer(4, 10);
        c.seat(); Order order = c.placeOrder(new Menu(), 10);
        eq(combo.name(), order.meal().name(), "customer independently chooses combo");
        order.prepare(10); order.prepare(11);
        eq(OrderStatus.PREPARING, order.status(), "combo needs all three units");
        order.prepare(12);
        eq(OrderStatus.READY, order.status(), "combo becomes ready after third unit");
    }

    private static void happyHourPolicy() {
        HappyHour policy = new HappyHour();
        policy.startTurn(1, true);
        eq(2, policy.remainingTurns(), "two active turns including activation");
        Customer vip = new VIPCustomer(2, 1); vip.seat();
        Order order = vip.placeOrder(new Menu(), policy, 1);
        eq(1080L, order.billCents(), "VIP then Happy Hour yields $10.80");
        policy.startTurn(2, false);
        eq(1, policy.remainingTurns(), "second active turn");
        policy.startTurn(3, false);
        eq(0, policy.remainingTurns(), "expired after two turns");
        eq(1500L, policy.apply(1500), "expired full price");
        eq(1080L, order.billCents(), "existing discounted order stays locked");
        try {
            policy.startTurn(4, true);
            throw new AssertionError("Expected invalid action: activation cannot repeat");
        } catch (RuleViolation expected) {
            checks++;
        }
        policy.startTurn(4, false);
        eq(0, policy.remainingTurns(), "failed activation does not consume turn");
        eq(1500L, PricingPolicy.STANDARD.apply(1500), "standard policy contract");

        Customer rounded = new Customer(9, 1) {
            @Override public String type() { return "Pricing fixture"; }
            @Override public int waitingLoss() { return 8; }
            @Override public MenuItem chooseMeal(Menu menu) { return new MenuItem("Eight cents", 8, 1); }
            @Override public long discountedBill(long cents) { return Money.percentage(cents, 90); }
        };
        HappyHour roundingPolicy = new HappyHour(); roundingPolicy.startTurn(1, true); rounded.seat();
        eq(6L, rounded.placeOrder(new Menu(), roundingPolicy, 1).billCents(), "round after VIP then Happy Hour in specified order");
    }

    private static void happyHourGameFlow() {
        GameEngine g = new GameEngine();
        begin(g); g.seatByWaiter(1, 1); end(g);
        g.beginTurn(true);
        eq(100, g.customer(1).satisfaction(), "recovery capped at 100 before decisions");
        g.hostWait(); g.takeOrder(1); g.prepare(1); g.finishTurn();
        eq(92, g.customer(1).satisfaction(), "active-turn waiting loss still applies");
        try {
            g.beginTurn(true);
            throw new AssertionError("Expected invalid action: repeated activation rejected by engine");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(2, g.turn(), "invalid activation does not advance turn");
        begin(g);
        eq(100, g.customer(1).satisfaction(), "second-turn recovery waiting for food");
        g.waiterWait(); g.prepare(1); g.finishTurn();
        begin(g);
        eq(92, g.customer(1).satisfaction(), "no recovery after expiry");
        g.serve(1); end(g); idle(g); idle(g);
        eq(1440L, g.stats().revenueCents(), "locked Happy Hour bill collected after expiry");

        GameEngine locked = new GameEngine();
        locked.beginTurn(); locked.seatByHost(1, 1); locked.takeOrder(1); locked.prepare(1); locked.finishTurn();
        locked.beginTurn(true); locked.hostWait(); locked.waiterWait(); locked.prepare(1); locked.finishTurn();
        eq(1800L, locked.order(1).billCents(), "activation does not change existing full-price order");

        Customer waiting = new RegularCustomer(1, 1); waiting.losePatience(); waiting.recoverSatisfaction(15);
        eq(100, waiting.satisfaction(), "waiting queue recovery");
        Customer served = new RegularCustomer(2, 1); served.losePatience(); served.seat();
        served.placeOrder(new Menu(), 1).prepare(1); served.serve(2); served.recoverSatisfaction(15);
        eq(92, served.satisfaction(), "eating customer receives no recovery");
        served.finishEating(3); served.recoverSatisfaction(15);
        eq(92, served.satisfaction(), "ready-to-pay customer receives no recovery");
        Receipt receipt = served.pay(4); served.recoverSatisfaction(15);
        eq(receipt.satisfaction(), served.satisfaction(), "paid customer receives no recovery");
        Customer left = new CriticCustomer(3, 1);
        for (int i = 0; i < 9; i++) { left.losePatience(); }
        left.recoverSatisfaction(15);
        eq(0, left.satisfaction(), "departed customer cannot recover");
        eq(CustomerStatus.LEFT, left.status(), "departed customer cannot rejoin");
    }
}
