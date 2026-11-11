# Stage 2 contribution log - Richard

> Historical contribution record: the commands, APIs, test counts and diagram baselines below describe the commits reviewed at that time. For current code and launch commands, use the repository README and `docs/test-notes.md`. The later course cleanup does not change the attribution in this record.

Date: 29 October 2026

Branch: `codex/richard-stage-2`

Reviewed code and diagram baseline: `ad77dfd246104d83051ac1661daa11d70a0a8500`

## Contribution in this batch

Reviewed the existing Stage 2 customer extension, checked the incoming Stage 1
integration, verified the combined game, and prepared the complete Stage 2
class diagram in Visual Paradigm 18. The Stage 2 implementation was already
present before this batch; this log records the review, verification and
diagram work rather than attributing the original implementation to this batch.

PRs [#4](https://github.com/n0nsense00/sc2002-restaurant-rush/pull/4) and
[#6](https://github.com/n0nsense00/sc2002-restaurant-rush/pull/6) were compared.
PR #6 included Wesley's Stage 1 changes and Zi Qi's consolidated integration,
preserved the Stage 2 implementation, and resolved the test entry-point
conflict. Richard squash merged #6 into the personal Stage 2 branch.
The reviewed head of #4 was already included in #6, so a separate merge of
#4 was unnecessary.

## Stage 2 additions and evolution from Stage 1

Stage 1's shared customer lifecycle and staff turn sequencing are retained.
Stage 2 extends that design with two subclasses and a reproducible mixed
arrival schedule.

| Type | Stage 2 behaviour or change | Reason |
| --- | --- | --- |
| `VIPCustomer` | Loses 5 satisfaction per unserved waiting turn, chooses Burger and pays 90% of the base price. | Adds different patience, preference and pricing through the customer contract. |
| `CriticCustomer` | Loses 12 satisfaction per unserved waiting turn and chooses Pasta. | Adds a distinct preference: a Regular customer with the same id, 3, would choose Salad. |
| `Scenario` | Creates C2 as VIP on turn 4, C3 as Critic on turn 7, and four Regular customers across six arrivals. | Makes the extension reproducible in the 18-turn base game. |
| `ConsoleApp` | Identifies the running game as Stage 2. | Keeps the displayed stage consistent with the implementation. |

`Customer.placeOrder` calls `chooseMeal` and `discountedBill`, while
`Customer.losePatience` calls `waitingLoss`. These calls dispatch through
customer objects. `GameEngine` continues to use the shared `Customer`
contract for the lifecycle instead of branching on customer subtype.

Cooking progress, later-turn serving, eating, payment, abandonment, staff
task limits, table release and the 18-turn result check were reviewed through
their existing domain and controller interactions. No production code,
public interfaces or test source were changed in this batch.

The 22 setup-validation and checked-money regression checks came through
the reviewed Stage 1 integration. They run alongside the original 78 Stage 2
checks, including the customer-polymorphism checks. These additions remain
attributed to the upstream integration.

## Build and regression evidence

Verification used a fresh local source snapshot of the pinned commit, with
all 34 copied files matched against their GitHub blob hashes before building.
This avoided leftover Stage 3 class files. Compilation used OpenJDK 21.0.12
with Java 17 compatibility and `-Xlint:all -Werror`.

Commands completed:

```powershell
.\build.ps1 test
java -jar build/restaurant-rush.jar --demo victory
java -jar build/restaurant-rush.jar --demo defeat
```

The suite passed **100 checks**. Both complete replay transcripts matched the
existing Stage 2 evidence after line-ending normalization; outcomes did not
change.

| Replay | Turns | Served | Paid | Unhappy departures | Revenue | Average paid satisfaction | Result |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| Victory | 18 | 5 | 4 | 0 | $67.50 | 70.25 | VICTORY |
| Defeat | 18 | 0 | 0 | 2 | $0.00 | N/A | DEFEAT |

Recorded evidence remains in
[`stage-2-victory.txt`](../evidence/stage-2-victory.txt),
[`stage-2-defeat.txt`](../evidence/stage-2-defeat.txt) and
[`test-notes.md`](../test-notes.md). No additional regression tests were
needed because this batch made no game-code corrections.

## Stage 2 class diagram

Prepared the editable
[`Restaurant_Rush_Stage2.vpp`](../uml/stage-2/Restaurant_Rush_Stage2.vpp)
project and matching SVG, PDF and PNG exports. The diagram was derived from
the verified Stage 2 source and retains all 21 Stage 1 production types plus
`VIPCustomer` and `CriticCustomer`.

The diagram contains **23 production types and 24 selected relationships**.
It includes the CLI boundary, turn controller, domain classes, staff roles,
result records and enums, with relevant fields, operations, visibility,
inheritance, ownership and multiplicities. Blue fill identifies the two new
customer subclasses; orange borders identify `Scenario` and `ConsoleApp`
as existing classes changed for Stage 2. Notes explain polymorphic dispatch
and the fixed mixed-customer schedule.

The native model was created, saved and reopened using Visual Paradigm
Standard 18.0. Its **49 selected fields, 57 methods and 20 enum literals**
were checked against the compiled classes. The exported native model was
also checked for class coverage, relationship endpoints, composition owners
and multiplicities. Native project integrity passed SQLite `quick_check`.
The PNG and rendered PDF were inspected, including the separated Table
multiplicity labels. Locks and local backup files were omitted.

Opening instructions, model-selection notes and artifact checksums are in
the diagram's [`README.md`](../uml/stage-2/README.md) and
[`verification.json`](../uml/stage-2/verification.json). The diagram generator
was adapted from the team's Stage 1 generator, with supporting source retained
under `docs/uml/stage-2/source/`.

## Files prepared for review

- `docs/changes/stage-2-richard.md`: this contribution and verification record.
- `docs/uml/stage-2/`: native project, exports, opening instructions and verification material.
- `README.md`: project documentation wording cleanup.

The diagram represents the pinned code baseline; these documentation additions
were prepared afterward. The outgoing PR compares `codex/richard-stage-2` against
base `codex/stage-3-work` for Elson's review.
