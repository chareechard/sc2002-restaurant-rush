# Restaurant Rush - Stage 2

The Stage 1 lifecycle plus VIP and Critic customer behaviour. This branch contains its own Java source, tests, replay inputs and UML artifacts.
The game builds offline with JDK 17 or newer; no external project repository is required.

## Build and play

On Windows, run `.\build.ps1 test` and `.\build.ps1 run` from this directory.
On Linux or macOS, run `sh build.sh test` and `sh build.sh run`.
Both create `build/restaurant-rush.jar`. For a saved game, use
`java -jar build/restaurant-rush.jar < replays/victory.txt` in a POSIX shell,
or `Get-Content replays/victory.txt | java -jar build/restaurant-rush.jar` in PowerShell.
Use `replays/defeat.txt` for the all-wait outcome. The current suite has 115 checks.

## Repository stages

The valid branches are `main`, `work/stage-1`, `work/stage-2` and `work/stage-3`.
`main` contains the final-stage project. Do not merge later-stage gameplay into an earlier-stage snapshot.
For reproducible source comparisons, use the full commit IDs in the [UML verification guide](docs/uml/verification-guide.md).

See [test notes](docs/test-notes.md), [course syntax and update instructions](docs/course-cleanup.md),
the [Stage 1 code guide](docs/stage-1-code-guide.md) and [reviewed Stage 1 class diagram (PDF)](docs/uml/stage-1/Restaurant_Rush_Stage1_Wesley_Draft.pdf).

Stage 2 also includes the [customer extension deliverables](docs/changes/stage-2-richard.md) and [reviewed Stage 2 class diagram (PDF)](docs/uml/stage-2/wesley-review/Stage_2_Class_Diagram.pdf).
