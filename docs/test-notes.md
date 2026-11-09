# Stage 1 verification

Code checkpoint: `991700154456e333083d8f2201f5504f6e519e4f`.
Run `./build.ps1 test` on Windows or `sh build.sh test` on Linux/macOS.

The current suite passes **106 checks**: the original 69, 22 earlier validation
regressions, and 15 cleanup checks covering copied lists, command tokenization
and money boundaries. The original `stage-1` tag remains the historical
69-check snapshot; `main` and the tags have not moved.

## Replay the game

Windows PowerShell:

```powershell
Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar
Get-Content replays/defeat.txt | java -jar build/restaurant-rush.jar
```

Linux/macOS:

```sh
java -jar build/restaurant-rush.jar < replays/victory.txt
java -jar build/restaurant-rush.jar < replays/defeat.txt
```

| Input file | Turns | Served | Paid | Unhappy | Revenue | Average paid satisfaction | Result |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| `replays/victory.txt` | 18 | 5 | 5 | 0 | $75.00 | 74.40 | VICTORY |
| `replays/defeat.txt` | 18 | 0 | 0 | 2 | $0.00 | N/A | DEFEAT |

Full stdin replay output was compared with the pre-cleanup `cb6d6df` code;
both transcripts are unchanged. The defeat input waits throughout the game.
Only C1 and C2 reach zero satisfaction by turn 18; C3-C6 remain waiting.

## What is checked

The suite follows the Pasta timeline: serve on turn 4, ready to pay on turn 5,
payment on turn 6 with satisfaction 76. It checks valid retries, per-role task
limits, serving/payment delays, table occupancy, cancellation, paid revenue,
stored satisfaction, the arrival schedule and both game outcomes.

Collection checks edit returned customer, queue, table, order, receipt, event
and menu lists and verify that game membership is unchanged. Input checks
cover malformed ids, extra arguments, a first-line BOM, comments, blank lines,
tabs, mixed-case commands and EOF. Money checks cover cent padding, rounding,
zero percentage and overflow boundaries. Expected errors use direct try/catch.
