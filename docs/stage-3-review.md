# Stage 3 review - 28 October 2026

## Current course cleanup

The Stage 3 source and tests match repository commit `f41ce70e652bf7a34a5a0b740b300e1e5d4ad01b`. The current suite has 181 checks. The implementation uses ordinary value classes, Scanner/PrintStream, traditional switches, Receipt-or-null and an explicit paid-average guard. See the [design discussion](design.md) and [UML verification guide](uml/verification-guide.md) for source scope and the checks actually performed. Native GUI reopening is pending.

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

The [Stage 3 class diagram](uml/stage-3/wesley-review/Stage_3_Class_Diagram.pdf) shows 31 production types and 28 relationships, including the RuleViolation helper. The [refined VIP service sequence](uml/stage-3/wesley-review/Restaurant_Rush_Stage3_Sequence_Refined.pdf) shows the manager’s turn-5 decision. Both editable projects are in the [Stage 3 diagram folder](uml/stage-3/wesley-review/README.md).

The sequence traces VIP C2 through arrival, Host seating, ordering, Happy Hour pricing, preparation, service, eating and payment, followed by the turn-18 outcome. It uses a turn loop and guarded fragments. Calls for unrelated customers are omitted; those customers follow the full replay. The starting conditions and both outcomes are part of the diagram.

| Decision at turn 5 | C2 served | C2 ready to pay | C2 pays | C2 satisfaction | Final average |
| --- | --- | --- | --- | --- | --- |
| Serve | 5 | 6 | 7 | 100 | 84 |
| Wait, then serve next turn | 6 | 7 | 8 | 95 | 83 |

Both full replays end after 18 turns with 6 served, 5 paid and $86.10 revenue. C2's bill remains $10.80. See `../replays/victory.txt`, `../replays/delayed-vip.txt`, `evidence/`, and `../src/test/java/rush/AllTests.java` for executable evidence.

## Group report

The [group report (PDF)](../output/pdf/Restaurant_Rush_MACS2_Grp3.pdf) includes the class diagram for each stage, the Stage 3 VIP service sequence and all four individual reflections.

See the [submission checklist](submission-readiness.md) for the remaining group tasks.
