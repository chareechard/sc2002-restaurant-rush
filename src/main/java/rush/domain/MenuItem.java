package rush.domain;

/** The fixed menu supplies the name, price in cents and required cooking time. */
public final class MenuItem {
    private final String name;
    private final long priceCents;
    private final int preparationUnits;

    public MenuItem(String name, long priceCents, int preparationUnits) {
        RuleViolation.require(name != null && !name.trim().isEmpty(), "A dish needs a name.");
        RuleViolation.require(priceCents > 0 && preparationUnits > 0, "Invalid menu item.");
        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public String name() { return name; }
    public long priceCents() { return priceCents; }
    public int preparationUnits() { return preparationUnits; }
}
