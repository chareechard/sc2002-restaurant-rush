# Reading the Stage 1 code

Stage 1 uses regular customers, three menu items, two tables and 18 turns.
Stage 2 retains this flow and adds VIP and Critic customer subclasses.

| File | What to read |
| --- | --- |
| `rush/cli/ConsoleApp.java` | `play()` runs the game; `chooseWaiter()` and `chooseChef()` read commands and retry invalid choices. |
| `rush/game/GameEngine.java` | `beginTurn()`, staff actions and `finishTurn()` run a turn in order. `stats()` totals paid receipts and checks the result. |
| `rush/game/Scenario.java` | Fixed settings and the arrival schedule. |
| `rush/domain/Customer.java` | Seating, ordering, eating, payment and satisfaction for one customer. |
| `rush/domain/RegularCustomer.java` | Regular meal choice and loss of 8 satisfaction per waiting turn. |
| `rush/domain/Order.java` | Cooking progress and PREPARING, READY, SERVED, PAID and CANCELLED states. |
| `rush/domain/Table.java` | The occupant; a table becomes free after payment or departure. |
| `rush/staff/Waiter.java`, `Chef.java`, `Cashier.java` | Staff tasks; the cashier finds the oldest eligible payer and collects one payment. |
| `rush/cli/ConsoleView.java` | Displays the state without changing it. |

## Follow one Pasta order

1. Turn 1: `seat 1 1` seats C1. The Chef waits.
2. Turn 2: `order 1` creates the Pasta order; `cook 1` completes its first unit.
3. Turn 3: the Waiter waits; `cook 1` finishes the dish, which becomes READY.
4. Turn 4: `serve 1` starts the customer's eating period.
5. Turn 5: the customer becomes READY_TO_PAY at turn end.
6. Turn 6: the cashier collects $18.00 and releases the table.

C1 loses 8 satisfaction on turns 1, 2 and 3, so payment records 76.
The automated checks follow this exact timeline in both stages.

## Java used in the current code

- `Scanner` reads console lines and separates a command into words. `PrintStream`
  displays output; normal play supplies `System.out`.
- Ordinary `switch` cases choose an action. Invalid choices throw an exception;
  the input loop catches it and repeats the same prompt.
- `List` and `ArrayList` store objects. Ordinary loops handle searches and totals.
  Getters build separate lists by adding the elements in a loop, so editing a
  returned list does not remove objects from the game. In Stages 1 and 2 the
  customer, order and table elements remain the existing domain objects; use
  `GameEngine` actions to follow the turn rules.
- Enums name fixed states such as READY and PAID.
- `MenuItem`, `Receipt` and `GameStats` are ordinary classes with constructors,
  private final fields and accessors. Their stored values cannot be reassigned.
- `Cashier.collect()` returns a `Receipt`, or `null` if nobody can pay. The engine
  checks for `null` before storing a payment.
- `GameStats.averageSatisfaction()` returns a `double`.
  `hasAverageSatisfaction()` checks whether anyone has paid; the display uses
  `N/A` when false. The stored zero in that case is not a paid customer's score.
- `Customer` defines shared behavior. Subclasses override preference, patience
  and pricing; Stage 2 uses these methods through the same Customer type.

## Money and validation

Money uses integer cents: 1800 means $18.00. Percentage calculations multiply
the cents by the percentage, add 50 and divide by 100 to round positive half
cents upward. Simple bounds checks before multiplication and addition reject
overflow. Display code separates dollars and cents and pads a single cent digit.

Table occupancy, customer/order states, staff phases, numeric input and the
serving/payment delays are assignment rules. Their checks run before an action
uses a staff task or changes state. Constructor checks also reject invalid ids,
missing references, negative bills and invalid menu data. These checks remain
because removing them would allow invalid gameplay or invalid objects.

The current versions keep 21 Stage 1 production types and 23 Stage 2 types.
Their class diagrams show the updated constructors, field types and selected
operations. See each stage's README for its code checkpoint and test count.
