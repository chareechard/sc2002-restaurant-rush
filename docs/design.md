# Restaurant Rush design discussion

This design implements the supplied SC2002 Restaurant Rush brief as three real Java snapshots. The final version runs the complete base game offline and adds customer specialisations, an independent Host task, composed meals and a timed pricing policy. Its decisions are reproducible rather than random, so a reviewer can trace an outcome back to specific manager commands.

## Responsibilities

| Abstraction | Responsibility and boundary |
| --- | --- |
| `ConsoleApp`, `ConsoleView` | Parse input and render state. They do not compute prices, alter satisfaction, determine eligibility or decide the result. Commands and validation errors cross the boundary through `GameEngine`. |
| `GameEngine` | Own the game aggregate, turn phases, per-role task budgets, arrival creation, end-of-turn sequencing and result calculation. Only it advances staff phases. |
| `Scenario` | Define the base setting and register the deterministic six arrivals. The only customer-type switch constructs objects here. |
| `Customer` and subclasses | Own the customer lifecycle and encapsulated state. Dynamic dispatch supplies patience, meal preference and customer-specific discount. |
| `Order` | Store chosen dish and immutable bill; track preparation, READY timing, serving, payment and cancellation. |
| `Table` | Hold at most one customer, validate seating and release only after payment or departure. |
| `Waiter`, `Chef`, `Cashier`, `Host` | Perform their narrow domain operations. Cashier selects the oldest eligible customer. Staff budgets belong to the controller's phase machine. |
| `Dish`, `MenuItem`, `ComboMeal`, `Menu` | Represent kitchen and billing data uniformly. A combo derives price and preparation from two immutable existing menu items. Menu returns a separate list built with a loop. |
| `PricingPolicy`, `HappyHour`, `Money` | Separate the price adjustment contract, timed promotional state and integer-cent arithmetic. Apply the customer discount before the restaurant policy. |
| `CustomerView`, `OrderView`, `TableView`, `Receipt`, `GameStats` | Immutable reporting values. The engine returns snapshots instead of mutable entities. Receipt captures satisfaction at payment; GameStats represents a computed view without changing the game. |

## Encapsulation, inheritance and composition

State fields are private. Domain transition methods reject invalid states, and immutable order bills prevent retrospective repricing. The engine keeps Customer, Order and Table instances private and returns CustomerView, OrderView and TableView objects with private final fields. Collection queries return new lists; changing a returned list does not change engine membership. Snapshot values cannot change. A caller cannot seat an engine customer or prepare an engine order through a query. OrderView contains a shared immutable MenuItem or ComboMeal from the engine menu. Domain transition methods remain public for staff collaborators and isolated domain fixtures; they do not provide access to engine-owned entities.

RegularCustomer, VIPCustomer and CriticCustomer inherit one lifecycle from Customer. The controller calls shared operations, and neither it nor the staff contain `instanceof` checks. The same Customer reference can choose different food, lose a different number of satisfaction points and produce a different bill without altering turn order.

GameEngine creates and owns its staff and tables; a Customer creates and owns its Order. These are UML composition relationships. ComboMeal uses object composition but shares immutable MenuItem components already present in Menu, so its UML relationship is shared aggregation rather than exclusive lifetime ownership. This distinction avoids implying that destroying a combo destroys the menu's Salad or Pasta.

## Principles grounded in the code

**SRP.** ConsoleView changes with presentation; Money changes with rounding conventions; Customer subclasses change with behaviour; GameEngine changes with turn scheduling. The controller still owns several related orchestration duties, which is reasonable for six customers but could be split if the game grows.

**OCP.** Stage 2 adds VIPCustomer and CriticCustomer plus two setup registrations. The core turn controller and staff files remain byte-for-byte unchanged. Stage 3 adds a genuine new workflow phase and passes a new pricing contract, so small changes to stable classes are necessary. The design does not claim that every extension requires zero edits.

**LSP.** Each customer subtype starts at 100, follows the same legal states and accepts the same lifecycle operations. Subtypes vary only the permitted patience, price and preference hooks. Existing timing tests run against the extended build. MenuItem and ComboMeal both supply positive preparation units and cent prices through Dish, so the Chef does not branch on dish type.

**ISP.** Dish exposes only name, price and preparation units. PricingPolicy exposes one adjustment operation. There is no unused Staff interface forcing a Host to implement cooking or payment.

**DIP.** Customer order creation and Waiter depend on PricingPolicy, and Order depends on Dish. HappyHour and ComboMeal implement those contracts. This is local dependency inversion, not a claim that the whole application is dependency-injected: GameEngine intentionally constructs its concrete staff and scenario as a small composition root.

## Timing and pricing choices

The explicit phases are BETWEEN_TURNS, HOST, WAITER, CHEF, END and FINISHED. Host acts before Waiter; this documented Stage 3 ordering permits seating and taking an order in the same turn. Waiter still acts before Chef. Phase changes occur only after successful actions, so invalid actions preserve the role's budget. A no-op wait intentionally consumes the role's task.

Orders have a readyTurn guard, customers have servedTurn and readyToPayTurn guards, and payment requires readiness strictly before the current turn. finishTurn advances eating first and then asks Cashier to select an eligible customer, preventing a customer who just finished eating from paying immediately. Ties for oldest payment readiness use arrival id. Abandonment happens after payment; READY but unserved food is also cancelled.

Happy Hour activation is validated before incrementing the turn or adding arrivals. Arrivals are created at 100, then eligible waiting customers recover up to 15 before staff decisions. Existing waiting customers, seated customers and customers waiting for food (including READY orders) are eligible. Served, paid and departed customers are excluded. Waiting loss still occurs at the end of each active turn. Activation on turn 18 is allowed, but the game still ends at turn 18.

Money is stored as long integer cents. Every positive percentage step rounds half up to the nearest cent: combo base price, then VIP discount if applicable, then Happy Hour. Thus Salad plus Pasta is 2430 cents and a VIP Burger in Happy Hour is 1080 cents. Fractional-cent tests distinguish the required discount order. Payment uses the price locked in the Order, even after Happy Hour ends.

## Meaningful choices

The Stage 3 victory replay activates Happy Hour on turn 4 and serves VIP C2 on turn 5. C2 pays on turn 7 with satisfaction 100. In `replays/delayed-vip.txt`, the manager waits on turn 5 and serves C2 on turn 6. The identical bill is paid on turn 8 with satisfaction 95, reducing the final average from 84.00 to 83.00 and keeping T2 occupied one more turn. Both still win; the changed timing and satisfaction are directly observable outcomes of a single service delay.

On turn 13 of the victory replay, Host seats C5 while Waiter serves C4's combo. This is a real capacity decision: the Waiter alone cannot both seat and serve. The all-wait replay produces Defeat with no paid customers. All runs use the same six arrivals, tables and 18-turn horizon; there is no hidden difficulty change or automatic victory.

## Trade-offs and alternative

An enum phase machine is easy to audit for this fixed sequence. The alternative was a State-pattern class per phase with injected commands. That could support configurable workflows but would spread a six-phase sequence across more files and obscure the simple one-task budget. The current guards have less indirection; adding many new roles would justify revisiting the choice.

Lists and explicit loops keep the small aggregate readable. Their linear searches are insignificant for six customers and two tables. The Stage 1 readability changes were carried through the Stage 2/3 integration while retaining immutable query snapshots. Deterministic preferences make regressions and recorded demonstrations repeatable, at the cost of replay variety. A seeded preference strategy could be added later without changing order timing.

The CLI, rule engine and tests have no third-party dependencies. A lightweight assertion runner keeps all mandatory testing offline. Its limitation is less detailed reporting than JUnit; failures still stop the build and name the violated expectation. Visual Paradigm and Python are report-authoring tools only and are not required to build or run the game.

Snapshot classes add small allocations and three reporting types. This is worthwhile for six customers and two tables because it makes the UI boundary enforceable by the compiler. An alternative would expose read-only interfaces backed by live entities; downcasting would still reach mutable objects, and values could change after a caller retained them. The current snapshots preserve the state at the moment of the query.

## Historical review corrections on 13 October 2026

Review of the assignment brief and actual code exposed three reproducibility problems. A UTF-8 byte-order mark was parsed as part of the first replay command; the reader now removes it only on the first physical line, and both comment-first and command-first victory replays are tested. That version also reported missing replay files concisely; the current version receives saved commands through standard input.

Build directories previously retained obsolete classes and resources. Both build scripts now recreate their generated class directories; dedicated Windows and Linux checks inject obsolete files and verify that the next JAR excludes them. Packaging rejects pending source changes and builds the JAR from the exact committed source archive in a fresh staging directory. This prevents a tested executable from disagreeing with the source delivered beside it.

The engine originally exposed mutable objects inside unmodifiable lists. Direct calls could bypass staff limits or seat a customer without assigning a table. Immutable reporting snapshots closed that path; the current ordinary classes retain that protection. Regression checks confirm retained snapshots do not change when the engine advances. The result predicate was also isolated in Scenario.targetsMet and tested at the exact payment, revenue and satisfaction thresholds, including independent failures of each target.

Each stage has one class diagram showing its controller, staff and domain types. Enumerations are explicitly labelled, and the customer-order ownership relationship is drawn once. The sequence uses creation messages, actual return values, activation bars and separate calls; notes carry state changes. For example, losePatience returns a boolean, while satisfaction 95 is an effect of the call, not its return value.

## Course syntax cleanup on 29 October 2026

The group requested source using Chapters 1-9, excluding Chapter 10. Ordinary final classes replace records; explicit receipt/null checks and a paid-count check replace Optional. Traditional switches, loops, explicit types and direct try/catch make each step visible. The standard pricing interface uses an ordinary anonymous implementation. The customer hierarchy, Dish and PricingPolicy remain because they have concrete uses in the required extensions.

Scanner reads commands and PrintStream prints results. The terminal can supply a saved command file through standard input; the Java program no longer opens replay files or bundles resources. ArrayList construction and manual copies replace list factories. Money formatting uses integer division, remainders and string concatenation; explicit bounds preserve overflow rejection. Paid averages still display two decimal places and N/A when no receipt exists.

The revised suites pass 106 checks for Stage 1, 115 for Stage 2 and 181 for Stage 3. Their saved standard-input runs retain the same gameplay and final results. The stage tags identify the simplified versions. The review used the supplied course notes as a reference for standard-library usage in the game source.

## Development reflections

The timing example was implemented before extensions. Its turn-4 service, turn-5 eating completion and turn-6 payment became a regression anchor. The first version's successful replay reaches five paid customers and exposes why table occupancy must continue until payment. Stage 2's Critic chooses a two-unit Pasta where a Regular would choose a one-unit Salad; the revised replay and expected totals demonstrate that the extension changes play, not merely a displayed label.

Stage 3 required the most purposeful edits: MenuItem became one implementation of Dish; order creation accepted PricingPolicy; GameEngine gained a Host phase and turn-start recovery. Customer lifecycle and payment rules stayed intact. The same core timeline tests pass with Host waiting and Happy Hour inactive. The differential VIP replay was added to provide evidence that a manager's choice changes a measured outcome.

These are observations about the implemented design and automated work, not claims about each member's personal development experience. Individual reflections and actual contributions should be added after the group reviews and understands the code.
