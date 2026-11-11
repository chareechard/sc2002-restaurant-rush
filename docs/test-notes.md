# Stage 2 verification

Code checkpoint: `bc769cc38beeccca023c0f71b8b347a425bcf6af`.
Run `./build.ps1 test` on Windows or `sh build.sh test` on Linux/macOS.

The current suite passes **115 checks**: the original 78, 22 earlier validation
regressions, and 15 cleanup checks covering copied lists, command tokenization
and money boundaries. Customer polymorphism and shared lifecycle checks remain.
The `stage-2` tag identifies the simplified 115-check version.

## Replay Stage 2

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
| `replays/victory.txt` | 18 | 5 | 4 | 0 | $67.50 | 70.25 | VICTORY |
| `replays/defeat.txt` | 18 | 0 | 0 | 2 | $0.00 | N/A | DEFEAT |

Full stdin replay output was compared with the pre-cleanup `e3fa9ce` code;
both transcripts are unchanged. Victory pays C1-C4; C5 is ready to pay when
the game ends. This is why served and paid counts differ. The defeat run has
two unhappy departures, C1 and C3.

C2 is a VIP: Burger costs $13.50 and waiting loses 5 satisfaction per turn.
C3 is a Critic: it chooses Pasta instead of the Salad a Regular C3 would
choose, and loses 12 satisfaction per waiting turn. Shared Customer methods
dispatch to these subclass behaviors. Nine explicit customer-behavior checks
run alongside the common timing and validation checks.

The cleanup checks verify independent returned lists, BOM/comments/blank lines,
tabs and mixed-case commands, and cent formatting and arithmetic boundaries.
Invalid inputs still retry without spending a staff action. Expected errors
use direct try/catch, and empty paid averages still display N/A.

## Stage 1 reference

The separately tested Stage 1 cleanup at
`991700154456e333083d8f2201f5504f6e519e4f` passes **106 checks**.
Its victory replay ends with 5 served, 5 paid, $75.00 revenue and average
74.40. Its defeat replay has 0 paid, $0.00, N/A average and two departures.
These are Stage 1 results from its own source and replay file, not the Stage 2
replay above. The `stage-1` tag identifies the simplified 106-check version.
