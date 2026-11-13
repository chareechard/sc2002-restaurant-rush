# Stage 3 review - 28 October 2026

## Current course cleanup

On 29 October 2026, source checkpoint `8344815` implements both cleanup rounds using Chapters 1-9. The current suite passes **181 checks**. It uses ordinary data classes, copied lists, Scanner/PrintStream, traditional switches and direct exception checks. Cashier returns Receipt or null; paid averages use a double plus hasAverageSatisfaction(). The three stdin replay outcomes are unchanged. The native models and exports were reviewed against these types. [Full changes and teammate pull instructions](course-cleanup.md).

The dated reviews below describe earlier checkpoints; their 155/177 counts and old record/Optional wording are historical.

The earlier review used the production source at historical checkpoint `02c75c9`. No gameplay changes were needed. The existing Stage 3 suite passed all 155 checks during this review.

## Code review

The review covered the turn controller, domain transitions, staff operations, customer subclasses, meal composition, pricing and console boundary. No blocking defect was found in the reviewed behaviour:

- Host, Waiter and Chef tasks advance through explicit phases. Failed validation leaves the current task available for retry.
- An order can be prepared in the turn it is placed, but can only be served after its READY turn. Eating completes after the service turn; payment requires an earlier READY_TO_PAY turn.
- Cashier selects at most one eligible customer, ordered by readiness and then customer id. A table stays occupied until payment or departure.
- VIP/Critic behaviour is selected through customer polymorphism. Combo preparation and price derive from its two immutable menu items.
- Happy Hour applies to the activation turn and the next turn, recovers eligible waiting customers, and applies its discount after the customer discount. An order's bill is immutable.
- Engine query methods return immutable snapshots. Victory is decided after all 18 turns, using paid receipts and the exact three targets.

This is a review of the current Stage 3 baseline. After integrating teammates' changes, rerun the destination branch tests and check the diagrams against the combined code.

## Diagram scope

Open `uml/Restaurant_Rush.vpp` in Visual Paradigm Desktop 18.0. Use **Stage 3 complete class diagram** and **Stage 3 VIP service choice - complete sequence** as this stage's primary diagrams. Earlier class detail views and sequence sheets are retained for reference.

The class diagram shows the 30 principal production types, key state and behaviour, inheritance, interface realisation, ownership and multiplicities. Routine getters, generated record methods, JDK types and the small `RuleViolation` validation helper are omitted to keep the diagram readable. Association ends represent object references that need not also appear as duplicate attribute rows.

The sequence traces VIP C2 through arrival, Host seating, ordering, Happy Hour pricing, preparation, service, eating and payment, followed by the turn-18 outcome. It uses a turn loop and guarded fragments. Calls for unrelated customers are omitted; those customers follow the full replay. The starting conditions and both outcomes are part of the diagram.

| Decision at turn 5 | C2 served | C2 ready to pay | C2 pays | C2 satisfaction | Final average |
| --- | --- | --- | --- | --- | --- |
| Serve | 5 | 6 | 7 | 100 | 84 |
| Wait, then serve next turn | 6 | 7 | 8 | 95 | 83 |

Both full replays end after 18 turns with 6 served, 5 paid and $86.10 revenue. C2's bill remains $10.80. See `../replays/victory.txt`, `../replays/delayed-vip.txt`, `evidence/`, and `../src/test/java/rush/AllTests.java` for executable evidence.

Native model checks verified all 30 classifier names against the Java source, 41 class relationships, and 34 messages across 12 lifelines and nine guarded fragments in the complete sequence. Missing message endpoints in the 93 older reference-sheet messages were repaired through Visual Paradigm's Open API. The refreshed XML export contains all 127 messages with valid source and target model references. The two new diagrams' saved geometry was unchanged after reopening and repairing the references. All 12 PDF pages were rendered and visually checked, including enlarged diagram details.

## Group completion still required

The briefing requires **three class diagrams: one for each stage**. The three older Stage 3 detail views do not satisfy that requirement. Wesley/Zi Qi supply the Stage 1 diagram, and Richard supplies the Stage 2 diagram, matching their reviewed code. The final group report must include those diagrams, all four members' own reviewed reflections and contributions, and confirmed submission details.

This branch remains a group-review draft. The Stage 3 tag and `main` are not moved by the diagram review. Nothing is submitted to NTULearn.
