package rush.domain;

import java.util.ArrayList;
import java.util.List;

public final class Menu {
    private final List<MenuItem> items = new ArrayList<MenuItem>();

    public Menu() {
        items.add(new MenuItem("Salad", 900, 1));
        items.add(new MenuItem("Burger", 1500, 1));
        items.add(new MenuItem("Pasta", 1800, 2));
    }

    public List<MenuItem> items() {
        List<MenuItem> copy = new ArrayList<MenuItem>();
        for (MenuItem item : items) {
            copy.add(item);
        }
        return copy;
    }

    public MenuItem named(String name) {
        for (MenuItem item : items) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        throw new RuleViolation("Unknown menu item: " + name);
    }
}
