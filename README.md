# Restaurant Rush

Restaurant Rush is an offline, turn-based Java restaurant management game for SC2002 Object Oriented Design and Programming. Manage staff, serve customers and meet the restaurant's targets before the end of turn 18.

## Quickstart

Install JDK 17 or newer with `java`, `javac` and `jar` on your PATH. Extract the ZIP before running the commands below. Building and playing require no third-party libraries or network connection.

Open a terminal in the extracted project directory.

Run on Windows with PowerShell:

```powershell
.\build.ps1 test
.\build.ps1 run
```

Or on Linux/macOS:

```sh
sh build.sh test
sh build.sh run
```

The scripts create `build/restaurant-rush.jar`. Run `java -jar build/restaurant-rush.jar --help` for launch help, or enter `help` or `status` during a game.

### Play a replay

After building, supply a saved game through standard input:

```powershell
# Windows PowerShell
Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar
```

```sh
# Linux/macOS
java -jar build/restaurant-rush.jar < replays/victory.txt
```

Replace `victory.txt` with `defeat.txt` or, in Stage 3, `delayed-vip.txt`. Blank lines and `#` comments are ignored; after end of input, remaining decisions default to `wait`.

## Playing the game

Each Stage 3 turn has four prompts: start (`happy` or `wait`), Host (`seat CUSTOMER TABLE` or `wait`), Waiter (`seat CUSTOMER TABLE`, `order CUSTOMER`, `serve ORDER` or `wait`) and Chef (`cook ORDER` or `wait`). Use numeric IDs, for example `seat 1 1` and `cook 1`. The Cashier collects payments automatically.

Win by the end of turn 18 with at least **4 paid customers, $45.00 revenue and 60 average paid satisfaction**. A newly completed dish cannot be served until a later turn; customers eat before paying, and tables stay occupied until payment or departure. Type `help` or `status` at any prompt, or see the [design discussion](docs/design.md) for timing, customer behaviour and pricing.

Customers choose meals deterministically. In Stage 3, a Regular whose ID is divisible by 4 chooses the Salad + Pasta combo; otherwise, the ID modulo 3 selects Pasta (1), Burger (2) or Salad (0). VIPs choose Burger and Critics choose Pasta.

## Project stages

| Stage | Branch | What's included | Checks |
| --- | --- | --- | ---: |
| 1 | `work/stage-1` | Regular customers and the core seating, ordering, cooking, service and payment flow. | 106 |
| 2 | `work/stage-2` | VIP and Critic customer subclasses. | 115 |
| 3 | `work/stage-3` | Host, combo meals and Happy Hour. | 181 |

The ZIP contains the final-stage files; the working branches can be built with the commands above when available in a Git checkout. Stage 1 and Stage 2 use Waiter and Chef prompts; Stage 3 adds the start and Host decisions. The implementation uses ordinary classes, explicit loops and traditional switches, with immutable Stage 3 views of game state.

## Individual AI chat histories

- [Elson’s chat history](https://github.com/chareechard/sc2002-restaurant-rush/tree/elson/ai-history)
- [Richard’s chat history](https://github.com/chareechard/sc2002-restaurant-rush/tree/richard/ai-history)
- [Wesley’s chat history](https://github.com/chareechard/sc2002-restaurant-rush/tree/wesley/ai-history)
- [Ran Ziqi’s chat history](https://github.com/chareechard/sc2002-restaurant-rush/tree/zi-qi/ai-history)

## Stage comparisons

The working branches `work/stage-1`, `work/stage-2` and `work/stage-3` represent the project stages. The ZIP is a file snapshot and does not contain Git branches or history.

### Stage 1 → Stage 2

Compare the Stage 1 and Stage 2 working branches in a Git checkout. Paths below are relative to `src/main/java/rush/` unless otherwise stated.

- **Added:** `domain/VIPCustomer.java` and `domain/CriticCustomer.java` implement the new customer behaviours.
- **Changed:** `game/Scenario.java` registers VIP and Critic arrivals; `cli/ConsoleApp.java` displays the Stage 2 label.
- **Tests and supporting files:** `src/test/java/rush/AllTests.java` covers the new behaviours; `replays/victory.txt` accommodates the Critic's Pasta; `README.md` and `docs/test-notes.md` describe the stage. New `docs/evidence/stage-2-*.txt` and `docs/uml/stage-2/` record the work, replay results and diagram artifacts.
- **Deleted:** none.

### Stage 2 → Stage 3

Compare the Stage 2 and Stage 3 working branches in a Git checkout. Paths below are relative to `src/main/java/rush/` unless otherwise stated.

- **Added:** `domain/Dish.java`, `domain/ComboMeal.java` and `domain/PricingPolicy.java` generalize meals and pricing; `game/HappyHour.java` implements the promotion; `staff/Host.java` seats customers; `game/CustomerView.java`, `game/OrderView.java` and `game/TableView.java` provide snapshots.
- **Changed:** `domain/Customer.java`, `RegularCustomer.java`, `VIPCustomer.java` and `CriticCustomer.java` support dishes, pricing or recovery; `domain/Menu.java`, `MenuItem.java` and `Order.java` support simple and composed dishes; `staff/Waiter.java` passes the pricing policy.
- **Changed:** `game/GameEngine.java` and `TurnPhase.java` add Host and promotion phases and expose snapshots; `Scenario.java` checks exact victory thresholds. `cli/ConsoleApp.java` and `ConsoleView.java` handle and display the new decisions and state. `domain/Receipt.java`, `game/GameStats.java` and `staff/Cashier.java` also have documentation or formatting changes.
- **Tests and supporting files:** `src/test/java/rush/AllTests.java`, `replays/victory.txt`, `README.md` and `docs/test-notes.md` are updated for Stage 3. New `replays/delayed-vip.txt` and `docs/evidence/stage-3-*.txt` demonstrate outcomes. The new `docs/design.md`, `docs/group.md`, `docs/stage-3-review.md`, `docs/video-script.md`, and `docs/uml/` Stage 3 artifacts provide design and submission evidence. The verification scripts, `.github/workflows/verify.yml`, `declarations/README.md` and `video/README.md` support testing and submission preparation; `.gitignore` is updated for generated files.
- **Deleted:** `docs/changes/stage-1-wesley.md`, `docs/changes/stage-2-richard.md`, `docs/course-cleanup.md` and `docs/uml/stage-1/source/render_diagram_pdf.py`.

The working branches provide the stage file lists, including diagram exports and model files.

## Video timestamps

**Recording pending.** Replace each placeholder with the actual timestamp in the completed group video.

| Segment required by the brief | Timestamp |
| --- | --- |
| Game launching from the submitted project | Pending |
| Meaningful manager choice with constrained staff | Pending |
| Dish progressing from READY to SERVED to payment | Pending |
| VIP or Critic behaviour | Pending |
| Stage 3 feature | Pending |
| Victory and defeat | Pending |
| Stage comparisons and diagrams | Pending |

## Docs

- [Design discussion](docs/design.md) and [test notes](docs/test-notes.md)
- Reviewed class diagrams (PDF) — [Stage 1](docs/uml/stage-1/Restaurant_Rush_Stage1_Wesley_Draft.pdf), [Stage 2](docs/uml/stage-2/wesley-review/Stage_2_Class_Diagram.pdf) and [Stage 3](docs/uml/stage-3/wesley-review/Stage_3_Class_Diagram.pdf)
- [Stage 3 sequence diagram](docs/uml/stage-3-sequence.svg) and [editable Visual Paradigm project](docs/uml/Restaurant_Rush.vpp)
- [Replay transcripts](docs/evidence/)

## Course project

SC2002 MACS2 Group 3: Tan Yong Han Elson, Tiew Zuo Yuan Richard, Wesley Yiap Wen Le and Ran Ziqi. Elson is responsible for Stage 3 integration. The recording, signed declarations and reviewed individual contributions remain outstanding; see the [group information](docs/group.md) and [recording plan](docs/video-script.md). The group report and packaging tools are not included in this ZIP.

No license file is present in the repository. Contact the repository owner for permission to reuse the code.

The [UML verification guide](docs/uml/verification-guide.md) lists source commits in this repository and explains the automated checks and remaining native GUI review.
