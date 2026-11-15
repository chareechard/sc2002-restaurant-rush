# Group recording script

These were the planned intervals for rehearsal. The [recording, presenter guide and measured timestamps](../README.md#video-timestamps) are in the root README.

| Planned interval | Screen and actions | Explanation to cover |
| --- | --- | --- |
| 00:00-01:30 | Show project files; run `./build.ps1 test` then `java -jar build/restaurant-rush.jar --help`. Linux/macOS uses `sh build.sh test`. | Introduce the base setting, offline JDK 17 build, group members and the game's three victory conditions. |
| 01:30-03:30 | Launch interactively. Turn 1: start `wait`, Host `seat 1 1`, Waiter `order 1`, Chef `cook 1`. Turn 2: start/Host `wait`; attempt `serve 1`, then valid `wait`; Chef `cook 1`. | Each staff role gets at most one task. C1 chooses Pasta, so the manager cannot reduce its preparation. An invalid serve explains the error without spending the Waiter task. The dish becomes READY only after the Chef acts. |
| 03:30-05:30 | Turn 3: `wait`, `wait`, `serve 1`, `wait`. Continue through turn 5 using the replay decisions. | Order READY on turn 2, SERVED on 3; eating finishes on 4; payment on 5. Table occupancy, paid revenue and recorded satisfaction illustrate temporal guards. |
| 05:30-07:00 | At turn 4 choose `happy`, Host `seat 2 2`, Waiter `order 2`, Chef `cook 2`. At turn 5 serve C2. Compare with Critic C3 at turn 7. | VIP patience 5, discount before Happy Hour: $15.00 to $13.50 to $10.80. On turn 5, recovery raises VIP satisfaction from 95 to 100. Critic always orders Pasta and loses 12, via polymorphism. |
| 07:00-09:00 | Show replay turns 10-15 and code for `ComboMeal`, `Host` and `HappyHour`. | C4 chooses Salad + Pasta Combo, $24.30 and 3 preparation units. On turn 13 Host seats C5 while Waiter serves C4. Happy Hour lasts exactly two turns, once per game; previously locked prices never change. |
| 09:00-10:45 | Run `Get-Content replays/victory.txt \| java -jar build/restaurant-rush.jar` and then pipe `replays/defeat.txt` into the same command. Show final summaries and `docs/evidence/stage-3-*.txt`. | Victory: 5 paid, 6 served, $86.10, 84.00 average. Defeat: no service, $0.00, N/A average, 2 departures. Both consume 18 turns; turn-16 customer cannot pay by the end. |
| 10:45-12:15 | Show `git log --oneline --decorate`; GitHub compare links and the production diff for Stage 1 to 2. | Three genuine runnable snapshots. Stage 2 changes customer creation and subclasses, leaving the turn controller unchanged. Stage 3 adds one phase and pricing/menu abstractions while retaining timing tests. |
| 12:15-14:15 | Open the three stage class diagrams and the refined Stage 3 sequence diagram using their SVG, PNG or diagram PDF files. | Visibility, multiplicities, inheritance, composition, Customer dispatch, Dish and PricingPolicy contracts. Walk the VIP from arrival to payment and explain the counterfactual waiting decision. |
| 14:15-15:00 | Show design discussion and actual test output. | Discuss the explicit enum state machine versus State classes, deterministic preferences and cent rounding. Summarise each member's actual reviewed contribution. |

## Useful commands

Git comparisons require a repository checkout with the stage branches; the ZIP itself has no Git history.

```powershell
git show origin/work/stage-1:src/main/java/rush/game/GameEngine.java
git diff origin/work/stage-1 origin/work/stage-2 -- src/main/java
git diff origin/work/stage-2 origin/work/stage-3 -- src/main/java
Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar
Get-Content replays/defeat.txt | java -jar build/restaurant-rush.jar
```

The interactive sequence above differs from the Stage 1 example because Stage 3's Host permits seating and ordering on the same turn. Use the Stage 1 working branch in a Git checkout to demonstrate the brief's original turn-6 Pasta payment.
