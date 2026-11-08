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
import rush.domain.CustomerStatus;
import rush.domain.Menu;
import rush.domain.MenuItem;
import rush.domain.Money;
import rush.domain.Order;
import rush.domain.OrderStatus;
import rush.domain.Receipt;
import rush.domain.RegularCustomer;
import rush.domain.RuleViolation;
import rush.domain.Table;
import rush.game.GameEngine;
import rush.game.GameResult;
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
        setupValidation();
        fullRuns();
        cliInput();
        defensiveCopies();
        System.out.println("PASS: " + checks + " checks (Stage 1)");
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
        try {
            g.order(1).serve(3);
            throw new AssertionError("Expected invalid action: newly ready dish cannot be served same turn");
        } catch (RuleViolation expected) {
            checks++;
        }
        g.finishTurn();
        eq(OrderStatus.READY, g.order(1).status(), "READY after 2 units");
        eq(76, g.customer(1).satisfaction(), "turn 3 satisfaction");
        begin(g); g.serve(1); end(g);
        eq(CustomerStatus.EATING, g.customer(1).status(), "served customer still eating");
        try {
            g.customer(1).pay(4);
            throw new AssertionError("Expected invalid action: cannot pay while eating");
        } catch (RuleViolation expected) {
            checks++;
        }
        eq(0L, g.stats().revenueCents(), "served is not revenue");
        begin(g); g.waiterWait(); end(g);
        eq(CustomerStatus.READY_TO_PAY, g.customer(1).status(), "full turn eating");
        try {
            g.customer(1).pay(5);
            throw new AssertionError("Expected invalid action: newly ready customer cannot pay immediately");
        } catch (RuleViolation expected) {
            checks++;
        }
        check(!g.table(1).isFree(), "table held until payment");
        begin(g); g.waiterWait(); end(g);
        eq(CustomerStatus.PAID, g.customer(1).status(), "payment turn 6");
        eq(OrderStatus.PAID, g.order(1).status(), "order marked paid");
        eq(1800L, g.stats().revenueCents(), "pasta bill");
        eq(76, g.receipts().get(0).satisfaction(), "record satisfaction at payment");
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
        try {
            g.table(1).release();
            throw new AssertionError("Expected invalid action: cannot release unpaid table");
        } catch (RuleViolation expected) {
            checks++;
        }
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
        customers.add(later);
        customers.add(earlier);
        List<Table> tables = new ArrayList<Table>();
        tables.add(t1);
        tables.add(t2);
        Receipt receipt = cashier.collect(customers, tables, 5);
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
        eq("$0.00", Money.format(0), "zero money display");
        eq("$0.09", Money.format(9), "single digit cents are padded");
        eq("$1.01", Money.format(101), "dollars and cents are separated");
        eq(0L, Money.percentage(Long.MAX_VALUE, 0), "zero percent does not overflow");
        eq(Long.MAX_VALUE / 100, Money.percentage(Long.MAX_VALUE - 50, 1), "largest safe rounded product");
        Scenario scenario = new Scenario();
        int arrivals = 0;
        for (int t = 1; t <= 18; t++) { if (scenario.hasArrival(t)) { arrivals++; } }
        eq(6, arrivals, "exactly six arrivals");
        GameEngine g = new GameEngine();
        List<Customer> customerCopy = g.customers();
        customerCopy.add(new RegularCustomer(99, 1));
        check(g.customers().isEmpty(), "adding to the returned list does not add a game customer");
        Customer paid = readyCustomer(2, 2); Receipt receipt = paid.pay(4);
        paid.losePatience();
        eq(receipt.satisfaction(), paid.satisfaction(), "paid satisfaction cannot decay");
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
        eq(7500L, win.stats().revenueCents(), "victory revenue");
        eq(74.4, win.stats().averageSatisfaction(), "victory average");
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

    private static void cliInput() {
        String input = "nonsense\nseat nope 1\nseat 1 3\nseat 1 1 extra\nseat 1 1\ncook 999\nwait\n";
        ByteArrayOutputStream transcript = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(transcript);
        Scanner commands = new Scanner(input);
        GameEngine g;
        try {
            g = new ConsoleApp(commands, output, true).play();
        } finally {
            commands.close();
            output.close();
        }
        check(transcript.toString().contains("Invalid choice:"), "CLI gives reason");
        eq(18, g.turn(), "EOF does not hang or end early");
        check(transcript.toString().contains("Waiter seats C1"), "valid retry after malformed input");
        check(transcript.toString().contains("Average paid satisfaction=N/A"), "N/A shown");

        ByteArrayOutputStream spacedTranscript = new ByteArrayOutputStream();
        PrintStream spacedOutput = new PrintStream(spacedTranscript);
        Scanner spacedCommands = new Scanner("\uFEFF# Replay\n\n  SeAt\t1   1  \nWaIt\n");
        GameEngine spacedGame;
        try {
            spacedGame = new ConsoleApp(spacedCommands, spacedOutput, true).play();
        } finally {
            spacedCommands.close();
            spacedOutput.close();
        }
        check(spacedTranscript.toString().contains("Waiter seats C1"), "BOM, blank lines, tabs and mixed-case commands are accepted");
        check(!spacedTranscript.toString().contains("Invalid"), "valid spaced replay needs no retry");
        eq(18, spacedGame.turn(), "spaced replay still runs all turns");
    }

    private static GameEngine replay(String name) throws IOException {
        Scanner commands = new Scanner(new File("replays/" + name + ".txt"));
        ByteArrayOutputStream transcript = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(transcript);
        try {
            GameEngine game = new ConsoleApp(commands, output, true).play();
            check(!transcript.toString().contains("Invalid"), name + " contains only legal choices");
            return game;
        } finally {
            commands.close();
            output.close();
        }
    }

    private static void defensiveCopies() {
        GameEngine game = new GameEngine();
        game.tables().clear();
        eq(2, game.tables().size(), "editing a returned table list does not remove the game's tables");
        game.beginTurn();
        game.customers().clear();
        eq(1, game.customers().size(), "editing a returned customer list does not remove customers");
        game.waitingQueue().clear();
        eq(1, game.waitingQueue().size(), "editing a returned waiting list does not remove queued customers");
        game.events().clear();
        check(!game.events().isEmpty(), "editing a returned event list does not remove game messages");
        game.seatByWaiter(1, 1); end(game);
        begin(game); game.takeOrder(1); game.prepare(1); game.finishTurn();
        game.orders().clear();
        eq(1, game.orders().size(), "editing a returned order list does not remove orders");
        begin(game); game.waiterWait(); game.prepare(1); game.finishTurn();
        begin(game); game.serve(1); end(game);
        idle(game); idle(game);
        game.receipts().clear();
        eq(1, game.receipts().size(), "editing a returned receipt list does not remove paid revenue");
        Menu menu = new Menu();
        menu.items().clear();
        eq(3, menu.items().size(), "editing a returned menu list does not change the fixed menu");
    }

    private static void begin(GameEngine g) { g.beginTurn(); }
    private static void end(GameEngine g) { g.chefWait(); g.finishTurn(); }
    private static void idle(GameEngine g) { begin(g); g.waiterWait(); end(g); }
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) { throw new AssertionError(message); }
    }
    private static void eq(Object expected, Object actual, String message) {
        check(expected.equals(actual), message + ": expected " + expected + ", got " + actual);
    }
}
