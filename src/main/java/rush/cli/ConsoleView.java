package rush.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import rush.domain.Money;
import rush.game.CustomerView;
import rush.game.GameEngine;
import rush.game.GameStats;
import rush.game.OrderView;
import rush.game.Scenario;
import rush.game.TableView;
import rush.game.TurnPhase;

/** Read-only rendering: no game state is changed here. */
public final class ConsoleView {
    private final PrintStream out;
    public ConsoleView(PrintStream out) { this.out = out; }

    public void state(GameEngine game) {
        out.println("\n=== TURN " + game.turn() + "/" + Scenario.MAX_TURNS + " ===");
        summary(game.stats());
        out.println("Happy Hour: " + game.happyHourRemaining() + " active turn(s) including this turn; "
            + (game.happyHourUsed() ? "activation used" : "activation available at the start of a turn"));
        List<String> waitingIds = new ArrayList<String>();
        for (CustomerView customer : game.waitingQueue()) {
            waitingIds.add("C" + customer.id());
        }
        out.println("Waiting queue: " + waitingIds);
        for (TableView table : game.tables()) {
            out.println("T" + table.id() + ": " + (table.isFree() ? "free" : "C" + table.customerId()));
        }
        for (CustomerView customer : game.customers()) {
            out.println("C" + customer.id() + " " + customer.type() + " | satisfaction="
                + customer.satisfaction() + " | " + customer.status());
        }
        for (OrderView order : game.orders()) {
            out.println("O" + order.id() + " for C" + order.customerId() + " | " + order.meal().name() + " | "
                + order.completedUnits() + "/" + order.meal().preparationUnits() + " | " + order.status()
                + " | locked bill " + Money.format(order.billCents()));
        }
        out.println("Current phase: " + game.phase());
        boolean host = game.phase() == TurnPhase.HOST;
        boolean waiter = host || game.phase() == TurnPhase.WAITER;
        boolean chef = waiter || game.phase() == TurnPhase.CHEF;
        out.println("Host [seat/wait]: " + availability(host) + " | Waiter [seat/order/serve/wait]: "
            + availability(waiter) + " | Chef [cook/wait]: " + availability(chef) + " | Cashier: automatic (max 1).");
        out.flush();
    }

    public void summary(GameStats stats) {
        String average = "N/A";
        if (stats.hasAverageSatisfaction()) {
            long rounded = (long) (stats.averageSatisfaction() * 100 + 0.5);
            long decimal = rounded % 100;
            average = (rounded / 100) + "." + (decimal < 10 ? "0" : "") + decimal;
        }
        out.println("Served=" + stats.servedCount() + " | Paid=" + stats.paidCount() + " | Unhappy="
            + stats.unhappyDepartures() + " | Revenue=" + Money.format(stats.revenueCents())
            + " | Average paid satisfaction=" + average + " | Turns=" + stats.turnsUsed());
        out.flush();
    }

    private String availability(boolean available) { return available ? "available" : "used/unavailable"; }
}
