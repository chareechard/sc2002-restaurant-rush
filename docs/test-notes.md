# Verification across all three stages

Run `./build.ps1 test` on Windows or `sh build.sh test` on Linux/macOS. The simplified Stage 1, 2 and 3 versions pass 106, 115 and 181 checks respectively. The stage tags identify these simplified versions; their game and test code matches the corresponding stage branches at the time of tagging.

| Stage / input file | Turns | Served | Paid | Unhappy | Revenue | Average | Result |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 / victory.txt | 18 | 5 | 5 | 0 | $75.00 | 74.40 | VICTORY |
| 2 / victory.txt | 18 | 5 | 4 | 0 | $67.50 | 70.25 | VICTORY |
| 3 / victory.txt | 18 | 6 | 5 | 0 | $86.10 | 84.00 | VICTORY |
| 3 / delayed-vip.txt | 18 | 6 | 5 | 0 | $86.10 | 83.00 | VICTORY |
| Each / defeat.txt | 18 | 0 | 0 | 2 | $0.00 | N/A | DEFEAT |

Use `Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar` in PowerShell, or `java -jar build/restaurant-rush.jar < replays/victory.txt` in a POSIX shell. Substitute the other files to reproduce their results. The removed --demo/--replay options are no longer valid. Complete standard-input output was compared against fresh pre-cleanup sources: all seven runs preserve the original output after line-ending normalization. Current transcripts are in docs/evidence/stage-N-*.txt.

## What is tested

- Original Pasta timing: Waiter seats turn 1, orders turn 2, serves turn 4; eating finishes turn 5; payment turn 6 at satisfaction 76. Stage 3 also verifies the Host's earlier seating/order path.
- Phase and lifecycle guards: invalid choices retain the staff task; no same-turn READY serving, early payment, duplicate staff action or premature table release.
- Setup, abandonment and accounting: invalid ids/data and null seating rejected; cancellation of PREPARING and READY orders; zero floor; paid receipts only; every run completes 18 turns.
- Customer polymorphism: VIP patience and discount, Critic preference and patience, unchanged shared lifecycle.
- Stage 3 additions: Host capacity, combo calculation, two-turn once-only Happy Hour, recovery cap/eligibility, normal waiting loss, discount ordering and locked bills.
- Input: Scanner word parsing, comments, whitespace, case-insensitive commands, malformed ids, help/status, retry, initial BOM and EOF default waits.
- Copied collections: editing a returned collection does not change engine/menu membership. Stage 3 additionally verifies retained customer/order/table snapshots remain unchanged.
- Arithmetic: integer cents, half-up rounding, explicit overflow bounds, valid boundaries and two-decimal output.

The previous branch counts were 91/100/177. Stage 1/2 add 15 focused checks; Stage 3 adds four overall. Existing read-only-list assertions now check copy isolation. Tests for deleted Java launch-file behavior are replaced by tests of the new command parser, rather than preserving an obsolete interface just to keep a test count.

Builds target Java 17 with all warnings treated as errors. Both build scripts remove stale generated classes. The final Stage 3 clean-rebuild helpers additionally inject obsolete files and verify they are absent after rebuilding. CI checks the current earlier-stage personal branches and the current proposal on Windows and Linux; consult the actual workflow run for its remote result.

## Group recording

Manually run the game and explain the visible dish, locked bill, preparation progress, staff availability, customer satisfaction and payment delay. Try an invalid id and a premature serve, then show that the same staff task remains available. These instructions are preparation for the group's recording; they do not claim it has been made.

Committed evidence transcripts normalize line endings and trailing prompt spaces; raw comparison outputs remain in the local build verification folder.
