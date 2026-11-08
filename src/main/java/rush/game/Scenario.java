package rush.game;

import rush.domain.Customer;
import rush.domain.RegularCustomer;

/** The reproducible base setting. Customer ids follow arrival order. */
public final class Scenario {
    public static final int MAX_TURNS = 18;
    public static final int TABLES = 2;
    public static final int MIN_PAID = 4;
    public static final long MIN_REVENUE_CENTS = 4500;
    public static final int MIN_SATISFACTION = 60;

    public boolean hasArrival(int turn) { return turn >= 1 && turn <= 16 && (turn - 1) % 3 == 0; }
    public Customer arrival(int turn) { return new RegularCustomer((turn - 1) / 3 + 1, turn); }
}
