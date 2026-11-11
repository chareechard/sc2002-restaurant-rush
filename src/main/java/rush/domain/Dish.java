package rush.domain;

/** The kitchen and billing need the same small contract for simple and composed meals. */
public interface Dish {
    String name();
    long priceCents();
    int preparationUnits();
}
