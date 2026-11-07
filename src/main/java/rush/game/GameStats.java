package rush.game;

/** Stores the totals displayed after each turn. */
public final class GameStats {
    private final int turnsUsed;
    private final int servedCount;
    private final int paidCount;
    private final int unhappyDepartures;
    private final long revenueCents;
    private final double averageSatisfaction;
    private final GameResult result;

    public GameStats(int turnsUsed, int servedCount, int paidCount, int unhappyDepartures,
                     long revenueCents, double averageSatisfaction, GameResult result) {
        this.turnsUsed = turnsUsed;
        this.servedCount = servedCount;
        this.paidCount = paidCount;
        this.unhappyDepartures = unhappyDepartures;
        this.revenueCents = revenueCents;
        this.averageSatisfaction = averageSatisfaction;
        this.result = result;
    }

    public int turnsUsed() { return turnsUsed; }
    public int servedCount() { return servedCount; }
    public int paidCount() { return paidCount; }
    public int unhappyDepartures() { return unhappyDepartures; }
    public long revenueCents() { return revenueCents; }
    public boolean hasAverageSatisfaction() { return paidCount > 0; }
    public double averageSatisfaction() { return averageSatisfaction; }
    public GameResult result() { return result; }
}
