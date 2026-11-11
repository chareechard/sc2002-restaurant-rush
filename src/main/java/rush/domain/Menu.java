package rush.domain;

import java.util.ArrayList;
import java.util.List;

public final class Menu {
    private final List<Dish> items;

    public Menu() {
        MenuItem salad = new MenuItem("Salad", 900, 1);
        MenuItem burger = new MenuItem("Burger", 1500, 1);
        MenuItem pasta = new MenuItem("Pasta", 1800, 2);
        items = new ArrayList<Dish>();
        items.add(salad);
        items.add(burger);
        items.add(pasta);
        items.add(new ComboMeal(salad, pasta));
    }

    public List<Dish> items() {
        List<Dish> copies = new ArrayList<Dish>();
        for (Dish item : items) {
            copies.add(item);
        }
        return copies;
    }

    public Dish named(String name) {
        for (Dish item : items) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        throw new RuleViolation("Unknown menu item: " + name);
    }
}
