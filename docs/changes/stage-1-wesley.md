# Stage 1 work log - Wesley branch

> Historical contribution record: the commands, APIs, test counts and diagram baselines below describe the commits reviewed at that time. For current code and launch commands, use the repository README and `docs/test-notes.md`. The later course cleanup does not change the attribution in this record.

## 2026-10-28 - Visual Paradigm class diagram draft

Source branch: `codex/wesley-stage-1`.
Source commit: `1b9ec925793af4bd88fca2aa05b660ad9bcccb06`.

Created `docs/uml/stage-1/Restaurant_Rush_Stage1_Wesley_Draft.vpp` using Visual
Paradigm Community Edition 18.1 and its public API. The editable native project
and its PDF, PNG, SVG and XML exports describe the existing Stage 1 implementation.
This is new diagram work, not a claim of implementing the historical game code.

The draft includes all 21 production types, selected members and 29 principal
relationships. It distinguishes the abstract Customer from RegularCustomer,
shows the derived waiting queue operation, and records the baseline color
convention for later stage comparisons. Scenario's creation dependency points to
the concrete RegularCustomer type.

Validation: Visual Paradigm saved and reopened the project, with 21 class shapes.
Compared the model's class set against the branch's production Java files.
Compared 49 selected fields, 49 selected methods and 20 enum literals against the
compiled Stage 1 code. Inspected the exported PDF and PNG. The branch's 69-check
baseline suite, victory replay and defeat replay passed at setup; this diagram
work does not change executable code or add tests.

The handoff assigns the shared Stage 1 diagram to Zi Qi, so this separately named
draft is ready for Zi Qi's review before any coordinated replacement of the
canonical project. No canonical diagram, test source, evidence, Java signature,
stage tag, final report or personal reflection was changed.

At Wesley's request, this diagram batch is being committed and pushed to
`origin/codex/wesley-stage-1`. It remains a draft for coordinated review; no pull
request or merge is included in this batch.

## 2026-10-28 - Simplify the Stage 1 implementation

Reviewed the supplied SC2002 Restaurant Rush assignment brief against the current
Stage 1 code at `7c533448a1d0cd905a8994a062e2e13017120a0e`. The user requested
code that is easier for a Year 2 student to follow while retaining Stage 1 behavior.

Replaced stream pipelines in customer/order/table lookup, queue display, menu
lookup, cashier selection and receipt totals with ordinary loops. Replaced the
nested result expression and the nested food-selection switch expression with
explicit control flow. Expanded command cases and state updates, used descriptive
local names, and renamed the private CLI argument-count helper for readability.

Removed seven RuleViolation guard calls on internally supplied setup values:
customer identity, order construction, table identity, null seating input, fixed
menu-item data (two calls) and percentage arguments. Replaced overflow-checking
percentage arithmetic with the ordinary rounded-cent formula for the game's
bounded amounts. Arbitrary invalid constructor values and overflowing amounts
are no longer checked; valid game data still follows the same rules.

Kept all checks that enforce player input, legal customer/order states, occupied
tables, the one-task-per-role limit, serving on a later turn, payment timing and
release only after payment/departure. Kept all 21 production types, their public
signatures, read-only collection access and the existing stage extension methods.
The implementation uses more lines for explicit control flow; the goal is easier
reading rather than fewer lines. No later-stage features were added.

Validation:

- `./build.ps1 test`: all 69 existing Stage 1 checks pass, including the Pasta
  timeline, invalid-action retries, task limits, abandonment and both endings.
- Compared 33 CLI runs with a freshly compiled copy of the pre-change commit:
  victory, defeat, help, invalid options, malformed ids, EOF, 12 victory runs with
  invalid retries and 12 mixed-decision runs. Exit codes, stdout and stderr match.
- Compared the public signatures of all 21 production types with `javap`; unchanged.
- Verified the class diagram's selected members against the compiled code.
- Victory remains 18 turns, 5 served, 5 paid, $75.00 and average 74.40. Defeat
  remains 18 turns, 0 paid, $0.00, 2 unhappy departures and average N/A.

Added `docs/stage-1-code-guide.md` and linked it from README. Existing tests and
evidence owned by Zi Qi are unchanged. The native UML project remains the original
draft snapshot; its key members and relationships still match because the public
structure did not change. No stage tags, final report or personal reflections
were changed.

## 2026-10-28 - Local PR review: restore validation

Following review of PR #5, restored the seven original RuleViolation guards in
Customer, Order, Table, MenuItem and Money, and restored checked percentage
arithmetic with Math.multiplyExact and Math.addExact. The earlier section records
the original refactor; this follow-up supersedes its validation-removal decision.
The ordinary loops, expanded command handling and descriptive names are retained.

Added 22 regression checks for invalid construction inputs, null seating without
occupancy changes, valid percentage boundaries, and intermediate arithmetic
overflow. The new tests failed against the uncorrected PR at the invalid customer
identity case, then passed after the guards were restored.

Validation: all 91 Stage 1 checks pass, git diff --check passes, and both CLI demos
retain their recorded results: victory has 5 paid, $75.00 and average 74.40;
defeat has 0 paid, $0.00, 2 unhappy departures and average N/A. Both run 18 turns.
Updated the code guide and test notes. Public signatures and UML structure are
unchanged. These corrections form a follow-up review revision for PR #5.

## 2026-10-28 - Simplify the Stage 1 UML drawing

Regenerated the editable Visual Paradigm draft against the reviewed Stage 1 code
at `6531bb58cd8d35d0cf0c17780f2727d97cf29806`. Retained all 21 production types,
49 selected fields, 49 selected methods and 20 enum literals.

Reduced the drawing from 29 to 19 relationships: removed four enum links and six
method/creation dependencies. Retained the three staff dependencies and changed
Cashier's dependency target from the returned Receipt to the Customer it charges.
Retained the structural associations, compositions and inheritance. Structural
navigability arrowheads are omitted, with reference types still shown in members.

Made Table-Customer multiplicity explicit as 0..1 at both ends, and Order-MenuItem
as 0..* orders to one menu item. Limited the engine's receipts to 0..5 for the
18-turn base game and made the remaining structural ends explicit. Reorganized
the layout with headings and a legend, and removed redundant association captions
that overlapped class headers.

Visual Paradigm saved and reopened the native project with 21 class shapes and
19 connectors. Checked the XML class set, member types, relationship counts,
inheritance direction, multiplicities and white Stage 1 class fills. The member
verifier passes against the compiled code. Rendered and visually inspected the
new PDF and PNG. No game source, tests, replays, evidence, stage tags or final
report were changed. This UML revision is committed to Wesley's branch for a
pull request into `codex/zi-qi-stage-1`.
