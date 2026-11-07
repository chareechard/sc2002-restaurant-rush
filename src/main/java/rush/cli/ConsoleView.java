package rush.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import rush.domain.Customer;
import rush.domain.Money;
import rush.domain.Order;
import rush.domain.Table;
import rush.game.GameEngine;
import rush.game.GameStats;
import rush.game.Scenario;
import rush.game.TurnPhase;

/** Read-only rendering: no game state is changed here. */
public final class ConsoleView {
    private final PrintStream out;
    public ConsoleView(PrintStream out) { this.out = out; }

    public void state(GameEngine game) {
        out.println("\n=== TURN " + game.turn() + "/" + Scenario.MAX_TURNS + " ===");
        summary(game.stats());
        List<String> waitingIds = new ArrayList<String>();
        for (Customer customer : game.waitingQueue()) {
            waitingIds.add("C" + customer.id());
        }
        out.println("Waiting queue: " + waitingIds);
        for (Table table : game.tables()) {
            out.println("T" + table.id() + ": " + (table.isFree() ? "free" : "C" + table.occupant().id()));
        }
        for (Customer customer : game.customers()) {
            out.println("C" + customer.id() + " " + customer.type() + " | satisfaction="
                + customer.satisfaction() + " | " + customer.status());
        }
        for (Order order : game.orders()) {
            out.println("O" + order.id() + " for C" + order.customer().id() + " | " + order.meal().name() + " | "
                + order.completedUnits() + "/" + order.meal().preparationUnits() + " | " + order.status()
                + " | locked bill " + Money.format(order.billCents()));
        }
        boolean waiterAvailable = game.phase() == TurnPhase.WAITER;
        boolean chefAvailable = game.phase() == TurnPhase.WAITER || game.phase() == TurnPhase.CHEF;
        out.println("Current phase: " + game.phase());
        out.println("Waiter [seat/order/serve/wait]: " + (waiterAvailable ? "available" : "used/unavailable")
            + " | Chef [cook/wait]: " + (chefAvailable ? "available" : "used/unavailable")
            + " | Cashier: automatic (max 1).");
        out.flush();
    }

    public void summary(GameStats stats) {
        String average = "N/A";
        if (stats.hasAverageSatisfaction()) {
            long rounded = (long) (stats.averageSatisfaction() * 100 + 0.5);
            long remainder = rounded % 100;
            average = (rounded / 100) + "." + (remainder < 10 ? "0" : "") + remainder;
        }
        out.println("Served=" + stats.servedCount() + " | Paid=" + stats.paidCount() + " | Unhappy="
            + stats.unhappyDepartures() + " | Revenue=" + Money.format(stats.revenueCents())
            + " | Average paid satisfaction=" + average + " | Turns=" + stats.turnsUsed());
        out.flush();
    }
}
