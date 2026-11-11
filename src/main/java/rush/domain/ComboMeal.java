package rush.domain;

/** A combo contains two existing menu items and derives its price and cooking time. */
public final class ComboMeal implements Dish {
    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        RuleViolation.require(first != null && second != null, "A combo needs two menu items.");
        this.first = first;
        this.second = second;
    }

    public MenuItem first() { return first; }
    public MenuItem second() { return second; }
    @Override public String name() { return first.name() + " + " + second.name() + " Combo"; }
    @Override public long priceCents() {
        if (first.priceCents() > Long.MAX_VALUE - second.priceCents()) {
            throw new ArithmeticException("Combo price overflow");
        }
        return Money.percentage(first.priceCents() + second.priceCents(), 90);
    }
    @Override public int preparationUnits() {
        if (first.preparationUnits() > Integer.MAX_VALUE - second.preparationUnits()) {
            throw new ArithmeticException("Combo preparation units overflow");
        }
        return first.preparationUnits() + second.preparationUnits();
    }
}
