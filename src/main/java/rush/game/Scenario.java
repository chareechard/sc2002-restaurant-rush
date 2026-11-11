package rush.game;

import rush.domain.Customer;
import rush.domain.CriticCustomer;
import rush.domain.RegularCustomer;
import rush.domain.VIPCustomer;

/** The reproducible base setting. Customer ids follow arrival order. */
public final class Scenario {
    public static final int MAX_TURNS = 18;
    public static final int TABLES = 2;
    public static final int MIN_PAID = 4;
    public static final long MIN_REVENUE_CENTS = 4500;
    public static final int MIN_SATISFACTION = 60;

    /** Compare exact integer totals so rounding the displayed average cannot change the result. */
    public static boolean targetsMet(int paidCount, long revenueCents, int satisfactionTotal) {
        return paidCount >= MIN_PAID && revenueCents >= MIN_REVENUE_CENTS
            && satisfactionTotal >= (long) MIN_SATISFACTION * paidCount;
    }

    public boolean hasArrival(int turn) { return turn >= 1 && turn <= 16 && (turn - 1) % 3 == 0; }
    public Customer arrival(int turn) {
        int id = (turn - 1) / 3 + 1;
        switch (id) {
            case 2: return new VIPCustomer(id, turn);
            case 3: return new CriticCustomer(id, turn);
            default: return new RegularCustomer(id, turn);
        }
    }
}
