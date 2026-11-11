package rush.cli;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import rush.domain.Dish;
import rush.domain.Money;
import rush.domain.RuleViolation;
import rush.game.GameEngine;
import rush.game.TurnPhase;

/** Reads commands and displays results. Game rules stay in the game and domain classes. */
public final class ConsoleApp {
    private final Scanner input;
    private final PrintStream out;
    private final boolean echo;
    private final ConsoleView view;
    private final GameEngine game = new GameEngine();
    private boolean eof;
    private boolean firstLine = true;
    private int printedEvents;

    public ConsoleApp(Scanner input, PrintStream out, boolean echo) {
        this.input = input;
        this.out = out;
        this.echo = echo;
        this.view = new ConsoleView(out);
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].equals("--help")) {
            System.out.println("Restaurant Rush: java -jar build/restaurant-rush.jar");
            System.out.println("Enter commands at the prompts, or redirect a replay text file into standard input.");
            return;
        }
        if (args.length != 0) {
            System.err.println("Invalid arguments. Use --help.");
            System.exit(2);
            return;
        }
        Scanner input = new Scanner(System.in);
        try {
            new ConsoleApp(input, System.out, false).play();
        } finally {
            input.close();
        }
    }

    public GameEngine play() {
        out.println("RESTAURANT RUSH - Stage 3 | Base setting | Offline Java");
        out.println("Win after 18 turns: >=4 paid, >=$45.00 revenue, >=60 average paid satisfaction.");
        for (Dish item : game.menu().items()) {
            out.println(item.name() + " " + Money.format(item.priceCents())
                + " (" + item.preparationUnits() + " preparation units)");
        }
        out.println("Commands use numeric ids (e.g. seat 1 1, order 1, cook 1, serve 1). Type help or status.");
        while (game.phase() != TurnPhase.FINISHED) {
            chooseTurnStart();
            printedEvents = 0;
            view.state(game);
            printNewEvents();
            chooseHost();
            printNewEvents();
            chooseWaiter();
            printNewEvents();
            chooseChef();
            game.finishTurn();
            printNewEvents();
            view.summary(game.stats());
        }
        out.println("\nRESULT: " + game.stats().result());
        view.summary(game.stats());
        return game;
    }

    private void printNewEvents() {
        List<String> events = game.events();
        while (printedEvents < events.size()) {
            out.println(events.get(printedEvents));
            printedEvents++;
        }
        out.flush();
    }

    private void chooseTurnStart() {
        while (game.phase() == TurnPhase.BETWEEN_TURNS) {
            String[] parts = command("Start turn " + (game.turn() + 1) + " [happy | wait]> ");
            try {
                checkArgumentCount(parts, 1);
                switch (parts[0]) {
                    case "happy":
                        game.beginTurn(true);
                        break;
                    case "wait":
                        game.beginTurn();
                        break;
                    case "help":
                    case "status":
                        view.state(game);
                        break;
                    default:
                        out.println("Invalid start command. Choose happy or wait.");
                        break;
                }
            } catch (IllegalArgumentException ex) { out.println("Invalid choice: " + ex.getMessage()); }
        }
    }

    private void chooseHost() {
        while (game.phase() == TurnPhase.HOST) {
            String[] parts = command("Host [seat C T | wait]> ");
            try {
                switch (parts[0]) {
                    case "wait":
                        checkArgumentCount(parts, 1); game.hostWait();
                        break;
                    case "seat":
                        checkArgumentCount(parts, 3); game.seatByHost(id(parts[1]), id(parts[2]));
                        break;
                    case "help":
                    case "status":
                        view.state(game);
                        break;
                    default:
                        out.println("Invalid Host command. Choose seat or wait.");
                        break;
                }
            } catch (IllegalArgumentException ex) { out.println("Invalid choice: " + ex.getMessage()); }
        }
    }

    private void chooseWaiter() {
        while (game.phase() == TurnPhase.WAITER) {
            String[] parts = command("Waiter [seat C T | order C | serve O | wait]> ");
            try {
                switch (parts[0]) {
                    case "wait":
                        checkArgumentCount(parts, 1);
                        game.waiterWait();
                        break;
                    case "seat":
                        checkArgumentCount(parts, 3);
                        game.seatByWaiter(id(parts[1]), id(parts[2]));
                        break;
                    case "order":
                        checkArgumentCount(parts, 2);
                        game.takeOrder(id(parts[1]));
                        break;
                    case "serve":
                        checkArgumentCount(parts, 2);
                        game.serve(id(parts[1]));
                        break;
                    case "help":
                    case "status":
                        view.state(game);
                        break;
                    default:
                        out.println("Invalid Waiter command. Choose seat, order, serve or wait.");
                        break;
                }
            } catch (IllegalArgumentException ex) {
                out.println("Invalid choice: " + ex.getMessage());
            }
        }
    }

    private void chooseChef() {
        while (game.phase() == TurnPhase.CHEF) {
            String[] parts = command("Chef [cook O | wait]> ");
            try {
                switch (parts[0]) {
                    case "wait":
                        checkArgumentCount(parts, 1);
                        game.chefWait();
                        break;
                    case "cook":
                        checkArgumentCount(parts, 2);
                        game.prepare(id(parts[1]));
                        break;
                    case "help":
                    case "status":
                        view.state(game);
                        break;
                    default:
                        out.println("Invalid Chef command. Choose cook or wait.");
                        break;
                }
            } catch (IllegalArgumentException ex) {
                out.println("Invalid choice: " + ex.getMessage());
            }
        }
    }

    private String[] command(String prompt) {
        out.print(prompt);
        out.flush();
        while (input.hasNextLine()) {
            String line = input.nextLine();
            if (firstLine) {
                firstLine = false;
                if (line.startsWith("\uFEFF")) { line = line.substring(1); }
            }
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (echo) {
                out.println(line);
            }
            Scanner words = new Scanner(line);
            List<String> tokens = new ArrayList<String>();
            while (words.hasNext()) {
                tokens.add(words.next());
            }
            words.close();
            if (tokens.isEmpty()) {
                continue;
            }
            String[] parts = new String[tokens.size()];
            for (int i = 0; i < tokens.size(); i++) {
                parts[i] = tokens.get(i);
            }
            String[] commands = { "happy", "wait", "help", "status", "seat", "order", "serve", "cook" };
            for (String known : commands) {
                if (parts[0].equalsIgnoreCase(known)) {
                    parts[0] = known;
                    break;
                }
            }
            return parts;
        }
        if (!eof) {
            out.println("End of input: remaining choices default to wait; all 18 turns still run.");
            eof = true;
        }
        return new String[] { "wait" };
    }

    private static void checkArgumentCount(String[] parts, int expected) {
        RuleViolation.require(parts.length == expected, "Wrong number of arguments. Type help.");
    }
    private static int id(String token) {
        try {
            int value = Integer.parseInt(token);
            RuleViolation.require(value > 0, "Ids must be positive integers.");
            return value;
        } catch (NumberFormatException ex) {
            throw new RuleViolation("Ids must be positive integers.");
        }
    }
}
