# Course syntax cleanup on 29 October 2026

Elson requested direct updates to all four personal branches, using Chapters 1-9 only. Chapter 10 is excluded. The update replaces unfamiliar syntax and selected library helpers while preserving the required gameplay. The stage tags now identify the simplified versions.

| Stage | Personal branches | Tested source checkpoint | Java checks |
| --- | --- | --- | ---: |
| 1 | `codex/wesley-stage-1`, `codex/zi-qi-stage-1` | `9917001` | 106 |
| 2 | `codex/richard-stage-2` | `bc769cc` | 115 |
| 3 | `codex/stage-3-work` | `8344815` | 181 |

## Code changes

- Ordinary classes with private final fields, constructors and accessors replace records.
- Cashier returns a Receipt or null; the engine checks for null. GameStats uses a double average and hasAverageSatisfaction(), so no paid customers still displays N/A.
- Traditional switches, explicit variable types, loops and direct try/catch replace arrow switches, var, streams, lambdas and method references. The small standard-pricing implementation uses an anonymous class, covered in Chapter 6.
- Collection constructors specify their element types, such as new ArrayList<Customer>(), instead of using the Chapter 10 diamond shorthand.
- Scanner reads input and command words. PrintStream, including System.out, prints output. The old reader/writer stack, explicit charset, regular-expression splitting and Locale calls are removed.
- ArrayList construction and add() replace List.of. A loop returns a separate list instead of List.copyOf. Changing that returned list cannot change the engine's collection. Stage 3 still returns immutable customer/order/table snapshots.
- Ordinary arithmetic and string concatenation format money and averages. Explicit bounds preserve overflow rejection without Math.addExact or Math.multiplyExact. Required rounding and the VIP-before-Happy-Hour discount order remain.
- Java no longer loads replay files or bundled resources. Files, Path, getResourceAsStream and the --demo/--replay options are removed from the game. Saved command text remains usable through standard input.
- Tests use explicit checks and simple input/output fixtures. Obsolete launch-file checks were replaced with checks of the new command parser; copy isolation and formatting boundaries are tested. Counts reflect the actual revised suites rather than preserving a count artificially.

All three victory and defeat outcomes remain unchanged; Stage 3's delayed VIP result remains unchanged too. Complete standard-input transcripts were compared with the original sources. Build scripts now clear generated classes for every stage and do not bundle replay resources into the JAR.

## Run the updated version

```powershell
.\build.ps1 test
.\build.ps1 run
Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar
Get-Content replays/defeat.txt | java -jar build/restaurant-rush.jar
```

For Stage 3's alternative decision, pipe `replays/delayed-vip.txt` in the same way.

```sh
sh build.sh test
java -jar build/restaurant-rush.jar < replays/victory.txt
```

The terminal supplies saved commands; the Java application only reads normal input. `--help` prints the launch instructions. End of input still supplies waits until all 18 turns finish.

## Teammates updating their local copy

Save your open Visual Paradigm project and preserve local edits first. Run `git status` before pulling. If there are local changes, commit them to your own branch or stash them, including untracked files, before integrating this update. Keep that saved work and review it after pulling; do not reset or force-push over it.

```powershell
git fetch origin
git switch YOUR-PERSONAL-BRANCH
git pull --ff-only origin YOUR-PERSONAL-BRANCH
.\build.ps1 test
```

Use your actual branch from the table above. If the fast-forward pull refuses because you have local commits, stop and coordinate their merge with Elson. Do not discard those commits. Wesley and Zi Qi start from the same updated Stage 1 source; agree who edits each source file and keep one editor for the Stage 1 .vpp file.

Future unrelated work can continue on the same four personal branches. Batch reviewed changes in Stage 1, then integrate them into Stage 2 and Stage 3. Never merge the entire Stage 3 project backwards into earlier stages. This direct update was specifically requested for the cleanup; it does not require new permanent shared branches.

## Artifacts and scope

Native Visual Paradigm projects, selected model descriptions, diagram exports and the report are updated with the new types and null-result convention. Class and sequence structure still represent the same gameplay. Personal contribution/reflection statements remain for the actual members to supply.

Optional Visual Paradigm generators, reflection verifiers, PDF tools and build scripts remain development tools outside the playable Java source. This cleanup removes the specifically reviewed language constructs and helpers; it does not claim that every remaining standard-library method is explicitly demonstrated in the lecture slides. The game has no third-party runtime dependency.

The stage tags and current branch sources identify the simplified versions to review. Draft packaging uses the stage branches and records their exact commits. Nothing has been submitted.

Committed evidence transcripts normalize line endings and trailing prompt spaces; raw comparison outputs remain in the local build verification folder.
