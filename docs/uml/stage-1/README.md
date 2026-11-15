# Stage 1 class diagram

This editable diagram represents repository source commit `02f5c5a46320fc3dc5bb4b5507b1dde5c2437bac`. Its production source and tests match `work/stage-1`.

Open the [reviewed class diagram (PDF)](Restaurant_Rush_Stage1_Wesley_Draft.pdf), [SVG](Restaurant_Rush_Stage1_Wesley_Draft.svg) or [PNG](Restaurant_Rush_Stage1_Wesley_Draft.png).
The [editable Visual Paradigm project](Restaurant_Rush_Stage1_Wesley_Draft.vpp) opens to **Stage 1 - Reviewed Class Diagram**.
The supplied PDF is the reference for the positions of all 21 classes and the routes of all 19 relationships.

## What changed

All 21 production types remain. The earlier Stage 1 review reduced the drawing
from 29 to **19 relationships**, arranged under CLI/control, customers/orders,
staff, menu, results, and enums/utilities headings. That relationship selection
is retained in this cleanup.

- Removed four enum connections. The enum boxes remain, and their types are
  visible in the corresponding attributes.
- Removed six method/creation dependencies. Parameter and return types still
  describe these uses in the key operation signatures.
- Retained three staff dependencies: Waiter uses Customer, Chef uses Order,
  and Cashier uses Customer. The cashier dependency previously pointed to the
  returned Receipt; the new target shows the object on which payment is performed.
- Retained inheritance, composition and the principal stored references.
- Omitted navigability arrowheads on structural associations. This does not
  assert bidirectional navigation; the attributes and operation types identify
  the actual references.

The 19 relationships comprise 11 compositions, four ordinary associations,
three dependencies and one generalization. The hollow inheritance triangle
points to the abstract Customer superclass. A filled diamond marks the owning
end of a composition; it does not describe execution order or Java garbage
collection. Orders reference menu items owned by Menu rather than owning those
shared menu items themselves.

## Multiplicities

| Relationship | Meaning |
| --- | --- |
| GameEngine `1` to Table `2` | Each game has two tables. |
| GameEngine `1` to Customer `0..6` | Up to six customers arrive. |
| GameEngine `1` to Order `0..6` | The engine indexes up to six orders. |
| GameEngine `1` to Receipt `0..5` | At most five customers can pay within 18 turns. |
| Customer `1` to Order `0..1` | A customer may have no order yet; an order belongs to one customer. |
| Table `0..1` to Customer `0..1` | A table can be empty and a customer can be waiting without a table. |
| Menu `1` to MenuItem `3` | The base menu owns the three fixed menu entries. |
| Order `0..*` to MenuItem `1` | Each order has one menu item; menu items can be referenced by multiple orders. |

Single-object compositions show `1` at both ends. Counts such as two tables and
six arrivals describe the base scenario. The Order end at MenuItem uses the
general many-orders relationship rather than the arrival schedule's tighter bound.

## Type and member coverage

- `rush.cli`: ConsoleApp, ConsoleView.
- `rush.game`: GameEngine, Scenario, GameStats, TurnPhase, GameResult.
- `rush.staff`: Waiter, Chef, Cashier.
- `rush.domain`: Customer, RegularCustomer, CustomerStatus, Order, OrderStatus,
  Table, Menu, MenuItem, Receipt, Money, RuleViolation.

Visibility, types, static scope and abstract status are recorded. MenuItem,
Receipt and GameStats are ordinary classes; their former record stereotypes
are removed. Enums use `enumeration`, and Customer remains abstract.

ConsoleApp uses Scanner and PrintStream; ConsoleView uses PrintStream.
Cashier.collect returns a Receipt or null. GameStats stores a double average
and supplies hasAverageSatisfaction(), which controls the N/A display.
These are the current member contracts. List getters return independent
ArrayList copies built with loops; they do not expose the backing lists.

The waiting queue is the derived `waitingQueue()` operation; there is no separate
queue class. Routine constructors and most accessors are omitted. RuleViolation's
JDK parent, IllegalArgumentException, is noted rather than drawn as another
application type. See `verification.json` for the selected member counts.

## Stage colors

| Appearance | Meaning |
| --- | --- |
| White class fill | Introduced in Stage 1 |
| Blue (`#D8EAFE`) | Reserved for Stage 2 additions |
| Green (`#DDF1DF`) | Reserved for Stage 3 additions |
| Orange border (`#D16D00`) | Existing class modified in a later stage |

All 21 classes in this Stage 1 diagram are white. The colored legend describes
the convention for future comparisons and does not introduce later-stage classes.

## Verification and editing

`verification.json` records the represented code checkpoint, native project
checks, selected-member comparison and artifact hashes. The source model and
exports must agree with the current code before a diagram change is committed.
The cleaned code passes 106 checks, with unchanged victory and defeat results.

The reviewed model uses [the member and relationship definitions](source/model.xml) and
[the diagram layout](source/layout.json). Its Visual Paradigm XML export is in
[model-xml/](model-xml/).

The PNG is a 3400 × 2402 rendering of the supplied PDF. The exported native class bounds and connector
routes were checked against the reviewed layout, and the SVG positions were checked
against the PDF. The filenames are shared across the project branches.

See [the verification record](verification.json) and [the UML verification guide](../verification-guide.md).
